package com.suprabanking.services.impl;

import com.suprabanking.config.security.CurrentUserService;
import com.suprabanking.models.RpaTask;
import com.suprabanking.models.User;
import com.suprabanking.repositories.RpaTaskRepository;
import com.suprabanking.services.NotificationService;
import com.suprabanking.services.RpaTaskService;
import com.suprabanking.services.dto.CreateRpaTaskRequest;
import com.suprabanking.services.dto.RpaTaskDTO;
import com.suprabanking.web.errors.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RpaTaskServiceImpl implements RpaTaskService {

    private static final String STATUS_PENDING = "EN_ATTENTE";
    private static final String STATUS_RUNNING = "EN_COURS";
    private static final String STATUS_DONE = "TERMINEE";
    private static final String STATUS_FAILED = "ECHEC";
    private static final String STATUS_CANCELLED = "ANNULEE";

    private final RpaTaskRepository rpaTaskRepository;
    private final CurrentUserService currentUserService;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public RpaTaskDTO createMyTask(CreateRpaTaskRequest request) {
        User user = currentUserService.getCurrentUser();

        RpaTask task = new RpaTask();
        task.setNomTache(request.getNomTache().trim());
        task.setType(request.getType().trim().toUpperCase());
        task.setPayload(request.getPayload());
        task.setStatut(STATUS_PENDING);
        task.setCreatedAt(LocalDateTime.now());
        task.setRetryCount(0);
        task.setUser(user);

        return toDto(rpaTaskRepository.save(task));
    }

    @Override
    public List<RpaTaskDTO> getMyTasks() {
        Long userId = currentUserService.getCurrentUser().getId();
        return rpaTaskRepository.findByUser_IdOrderByCreatedAtDesc(userId).stream().map(this::toDto).toList();
    }

    @Override
    @Transactional
    public void cancelMyTask(Long taskId) {
        Long userId = currentUserService.getCurrentUser().getId();
        RpaTask task = rpaTaskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Tâche RPA introuvable"));

        if (task.getUser() == null || !userId.equals(task.getUser().getId())) {
            throw new ResourceNotFoundException("Tâche RPA introuvable");
        }

        if (STATUS_DONE.equals(task.getStatut()) || STATUS_RUNNING.equals(task.getStatut())) {
            throw new IllegalArgumentException("Impossible d'annuler une tâche déjà en cours ou terminée");
        }

        task.setStatut(STATUS_CANCELLED);
        task.setFinishedAt(LocalDateTime.now());
        task.setResultMessage("Tâche annulée par l'utilisateur");
        rpaTaskRepository.save(task);
    }

    @Override
    public List<RpaTaskDTO> getPendingTasks() {
        return rpaTaskRepository.findByStatutOrderByCreatedAtAsc(STATUS_PENDING).stream().map(this::toDto).toList();
    }

    @Override
    public List<RpaTaskDTO> getAllTasks() {
        return rpaTaskRepository.findAll().stream().map(this::toDto).toList();
    }

    @Override
    @Transactional
    public RpaTaskDTO executeTaskNow(Long taskId) {
        RpaTask task = rpaTaskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Tâche RPA introuvable"));

        executeSingleTask(task);
        return toDto(task);
    }

    @Override
    @Transactional
    public int executePendingTasksBatch() {
        List<RpaTask> pendingTasks = rpaTaskRepository.findByStatutOrderByCreatedAtAsc(STATUS_PENDING);
        int executed = 0;
        for (RpaTask task : pendingTasks) {
            executeSingleTask(task);
            executed++;
        }
        return executed;
    }

    @Scheduled(fixedDelayString = "${app.rpa.execution-delay-ms:30000}")
    @Transactional
    public void orchestratePendingTasks() {
        List<RpaTask> pendingTasks = rpaTaskRepository.findByStatutOrderByCreatedAtAsc(STATUS_PENDING);
        if (pendingTasks.isEmpty()) {
            return;
        }

        log.debug("RPA scheduler picked {} pending tasks", pendingTasks.size());
        for (RpaTask task : pendingTasks) {
            executeSingleTask(task);
        }
    }

    private void executeSingleTask(RpaTask task) {
        if (STATUS_CANCELLED.equals(task.getStatut()) || STATUS_DONE.equals(task.getStatut())) {
            return;
        }

        task.setStatut(STATUS_RUNNING);
        task.setStartedAt(LocalDateTime.now());
        rpaTaskRepository.save(task);

        try {
            String result = simulateRpaExecution(task.getType(), task.getPayload());
            task.setStatut(STATUS_DONE);
            task.setDateExecution(LocalDateTime.now());
            task.setFinishedAt(LocalDateTime.now());
            task.setResultMessage(result);
            task.setLastError(null);
            rpaTaskRepository.save(task);

            notifyTaskResult(task, true);
        } catch (Exception ex) {
            int retries = task.getRetryCount() == null ? 0 : task.getRetryCount();
            task.setRetryCount(retries + 1);
            task.setStatut(STATUS_FAILED);
            task.setFinishedAt(LocalDateTime.now());
            task.setLastError(ex.getMessage());
            task.setResultMessage("Exécution RPA échouée");
            rpaTaskRepository.save(task);

            notifyTaskResult(task, false);
        }
    }

    private String simulateRpaExecution(String type, String payload) {
        String normalizedType = type == null ? "GENERIC" : type.toUpperCase();
        return switch (normalizedType) {
            case "RAPPROCHEMENT_COMPTES" -> "Rapprochement comptable terminé avec succès";
            case "EXPORT_AUDIT" -> "Export des audits terminé (fichier généré)";
            case "ANALYSE_FRAUDE" -> "Analyse anti-fraude exécutée, aucun blocage critique";
            case "NOTIFICATION_LOT" -> "Envoi en lot des notifications terminé";
            default -> "Tâche RPA générique exécutée (payload=" + (payload == null ? "" : payload) + ")";
        };
    }

    private void notifyTaskResult(RpaTask task, boolean success) {
        if (task.getUser() == null || task.getUser().getClient() == null || task.getUser().getClient().getId() == null) {
            return;
        }

        Long clientId = task.getUser().getClient().getId();
        String content = success
                ? "RPA: tâche '" + task.getNomTache() + "' terminée avec succès"
                : "RPA: échec de la tâche '" + task.getNomTache() + "'";
        notificationService.createForClient(clientId, content);
    }

    private RpaTaskDTO toDto(RpaTask task) {
        return RpaTaskDTO.builder()
                .id(task.getId())
                .nomTache(task.getNomTache())
                .type(task.getType())
                .statut(task.getStatut())
                .payload(task.getPayload())
                .resultMessage(task.getResultMessage())
                .lastError(task.getLastError())
                .retryCount(task.getRetryCount())
                .createdAt(task.getCreatedAt())
                .startedAt(task.getStartedAt())
                .finishedAt(task.getFinishedAt())
                .dateExecution(task.getDateExecution())
                .userId(task.getUser() != null ? task.getUser().getId() : null)
                .username(task.getUser() != null ? task.getUser().getUsername() : null)
                .build();
    }
}
