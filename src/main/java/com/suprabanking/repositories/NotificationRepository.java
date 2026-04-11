package com.suprabanking.repositories;

import com.suprabanking.models.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByStatut(String statut);
    List<Notification> findByClientId(Long clientId);
    List<Notification> findByClientIdOrderByDateEnvoiDesc(Long clientId);
    List<Notification> findByClientIdAndStatutOrderByDateEnvoiDesc(Long clientId, String statut);
    long countByClientIdAndStatut(Long clientId, String statut);
    long deleteByClientIdAndStatut(Long clientId, String statut);
    Optional<Notification> findByIdAndClientId(Long id, Long clientId);
}
