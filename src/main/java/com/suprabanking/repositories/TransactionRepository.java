package com.suprabanking.repositories;

import com.suprabanking.models.Transaction;
import com.suprabanking.models.Compte;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByCompte(Compte compte);
    List<Transaction> findByType(String type);
    List<Transaction> findByCompteId(Long compteId);
    List<Transaction> findByClient_Id(Long clientId);
    Page<Transaction> findByClient_Id(Long clientId, Pageable pageable);
    Optional<Transaction> findByIdAndClient_Id(Long id, Long clientId);

        @Query("""
            select t from Transaction t
            where t.client.id = :clientId
              and t.compte.id = :compteId
              and (:type is null or lower(t.type) = lower(:type))
              and (:dateFrom is null or t.dateTransaction >= :dateFrom)
              and (:dateTo is null or t.dateTransaction <= :dateTo)
              and (:montantMin is null or t.montant >= :montantMin)
              and (:montantMax is null or t.montant <= :montantMax)
            """)
        Page<Transaction> findMyTransactionsByCompteWithFilters(
            @Param("clientId") Long clientId,
            @Param("compteId") Long compteId,
            @Param("type") String type,
            @Param("dateFrom") LocalDateTime dateFrom,
            @Param("dateTo") LocalDateTime dateTo,
            @Param("montantMin") Double montantMin,
            @Param("montantMax") Double montantMax,
            Pageable pageable
        );
}
