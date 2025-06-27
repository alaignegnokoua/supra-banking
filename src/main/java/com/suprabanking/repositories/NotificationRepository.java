package com.suprabanking.repositories;

import com.suprabanking.models.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByStatut(String statut);
    List<Notification> findByClientId(Long clientId);
}
