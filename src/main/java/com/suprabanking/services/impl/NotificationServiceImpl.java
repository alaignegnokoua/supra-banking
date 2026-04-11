package com.suprabanking.services.impl;

import com.suprabanking.config.security.CurrentUserService;
import com.suprabanking.models.Client;
import com.suprabanking.models.Notification;
import com.suprabanking.repositories.ClientRepository;
import com.suprabanking.repositories.NotificationRepository;
import com.suprabanking.services.NotificationService;
import com.suprabanking.services.dto.NotificationDTO;
import com.suprabanking.web.errors.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private static final String STATUT_NON_LU = "NON_LU";
    private static final String STATUT_LU = "LU";

    private final NotificationRepository notificationRepository;
    private final ClientRepository clientRepository;
    private final CurrentUserService currentUserService;

    @Override
    public List<NotificationDTO> findMyNotifications() {
        Long clientId = currentUserService.requireCurrentClientId();
        return notificationRepository.findByClientIdOrderByDateEnvoiDesc(clientId)
                .stream().map(this::toDto).toList();
    }

    @Override
    public NotificationDTO markAsRead(Long id) {
        Long clientId = currentUserService.requireCurrentClientId();
        Notification notification = notificationRepository.findByIdAndClientId(id, clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification introuvable"));
        notification.setStatut(STATUT_LU);
        return toDto(notificationRepository.save(notification));
    }

    @Override
    public void createForClient(Long clientId, String contenu) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client introuvable pour notification"));

        Notification notification = new Notification();
        notification.setContenu(contenu);
        notification.setDateEnvoi(LocalDateTime.now());
        notification.setStatut(STATUT_NON_LU);
        notification.setClient(client);
        notificationRepository.save(notification);
    }

    private NotificationDTO toDto(Notification n) {
        return new NotificationDTO(n.getId(), n.getContenu(), n.getDateEnvoi(), n.getStatut());
    }
}
