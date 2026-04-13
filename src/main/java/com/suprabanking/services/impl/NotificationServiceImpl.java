package com.suprabanking.services.impl;

import com.suprabanking.config.security.CurrentUserService;
import com.suprabanking.models.Client;
import com.suprabanking.models.Notification;
import com.suprabanking.repositories.ClientRepository;
import com.suprabanking.repositories.NotificationRepository;
import com.suprabanking.services.EmailNotificationService;
import com.suprabanking.services.ExternalNotificationService;
import com.suprabanking.services.NotificationService;
import com.suprabanking.services.dto.NotificationDTO;
import com.suprabanking.web.errors.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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
    private final EmailNotificationService emailNotificationService;
    private final ExternalNotificationService externalNotificationService;
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
    public int markAllMyNotificationsAsRead() {
        Long clientId = currentUserService.requireCurrentClientId();
        List<Notification> unread = notificationRepository.findByClientIdAndStatutOrderByDateEnvoiDesc(clientId, STATUT_NON_LU);

        if (unread.isEmpty()) {
            return 0;
        }

        unread.forEach(n -> n.setStatut(STATUT_LU));
        notificationRepository.saveAll(unread);
        return unread.size();
    }

    @Override
    public long countMyUnreadNotifications() {
        Long clientId = currentUserService.requireCurrentClientId();
        return notificationRepository.countByClientIdAndStatut(clientId, STATUT_NON_LU);
    }

    @Override
    public void deleteMyNotification(Long id) {
        Long clientId = currentUserService.requireCurrentClientId();
        Notification notification = notificationRepository.findByIdAndClientId(id, clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification introuvable"));
        notificationRepository.delete(notification);
    }

    @Override
    @Transactional
    public long deleteMyReadNotifications() {
        Long clientId = currentUserService.requireCurrentClientId();
        return notificationRepository.deleteByClientIdAndStatut(clientId, STATUT_LU);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createForClient(Long clientId, String contenu) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client introuvable pour notification"));

        if (Boolean.FALSE.equals(client.getNotificationsInAppEnabled())) {
            log.debug("In-app notifications disabled for client {}, skipping notification creation", clientId);
        } else {
            Notification notification = new Notification();
            notification.setContenu(contenu);
            notification.setDateEnvoi(LocalDateTime.now());
            notification.setStatut(STATUT_NON_LU);
            notification.setClient(client);
            notificationRepository.save(notification);
        }

        if (Boolean.TRUE.equals(client.getNotificationsEmailEnabled())) {
            emailNotificationService.sendNotificationEmail(client, contenu);
        }

        if (Boolean.TRUE.equals(client.getNotificationsSmsEnabled())) {
            externalNotificationService.sendSmsNotification(client, contenu);
        }

        if (Boolean.TRUE.equals(client.getNotificationsTelegramEnabled())) {
            externalNotificationService.sendTelegramNotification(client, contenu);
        }
    }

    private NotificationDTO toDto(Notification n) {
        return new NotificationDTO(n.getId(), n.getContenu(), n.getDateEnvoi(), n.getStatut());
    }
}
