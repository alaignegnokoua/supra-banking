package com.suprabanking.repositories;

import com.suprabanking.models.Beneficiaire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BeneficiaireRepository extends JpaRepository<Beneficiaire, Long> {
    List<Beneficiaire> findByClient_Id(Long clientId);
    Optional<Beneficiaire> findByIdAndClient_Id(Long id, Long clientId);
    boolean existsByClient_IdAndIban(Long clientId, String iban);
    boolean existsByClient_IdAndRib(Long clientId, String rib);

    @Query("""
        select count(b)
        from Beneficiaire b
        where b.createdAt >= :startDate
          and b.createdAt <= :endDate
        """)
    long countByCreatedAtBetween(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    @Query("""
        select count(b)
        from Beneficiaire b
        where b.lastUsedAt >= :date
        """)
    long countByLastUsedAtAfter(@Param("date") LocalDateTime date);
}
