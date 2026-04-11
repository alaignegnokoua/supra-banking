package com.suprabanking.services;

import com.suprabanking.services.dto.NotificationDTO;

import java.util.List;

public interface NotificationService {
    List<NotificationDTO> findMyNotifications();
    NotificationDTO markAsRead(Long id);
    int markAllMyNotificationsAsRead();
    long countMyUnreadNotifications();
    void deleteMyNotification(Long id);
    long deleteMyReadNotifications();
    void createForClient(Long clientId, String contenu);
}
