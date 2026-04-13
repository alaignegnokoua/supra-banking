package com.suprabanking.repositories;

import com.suprabanking.models.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
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

    long countByReadFalse();

    @Query("""
        select count(n)
        from Notification n
        where n.dateEnvoi >= :startDate
          and n.dateEnvoi <= :endDate
        """)
    long countByCreatedAtBetween(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
}
