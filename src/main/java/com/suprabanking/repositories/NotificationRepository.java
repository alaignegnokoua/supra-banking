package com.suprabanking.repositories;

import com.suprabanking.models.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByStatut(String statut);
    List<Notification> findByClientId(Long clientId);
    List<Notification> findByClientIdOrderByDateEnvoiDesc(Long clientId);
    Optional<Notification> findByIdAndClientId(Long id, Long clientId);
}
