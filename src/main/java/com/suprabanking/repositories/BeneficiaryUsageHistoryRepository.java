package com.suprabanking.repositories;

import com.suprabanking.models.BeneficiaryUsageHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface BeneficiaryUsageHistoryRepository extends JpaRepository<BeneficiaryUsageHistory, Long> {

    @Query("""
        SELECT h FROM BeneficiaryUsageHistory h
        WHERE h.beneficiaire.client.id = :clientId
        AND h.beneficiaire.id = :beneficiaireId
        ORDER BY h.usedAt DESC
    """)
    Page<BeneficiaryUsageHistory> findByBeneficiaireAndClient(
            @Param("beneficiaireId") Long beneficiaireId,
            @Param("clientId") Long clientId,
            Pageable pageable
    );

    @Query("""
        SELECT h FROM BeneficiaryUsageHistory h
        WHERE h.beneficiaire.client.id = :clientId
        ORDER BY h.usedAt DESC
    """)
    Page<BeneficiaryUsageHistory> findByClient(
            @Param("clientId") Long clientId,
            Pageable pageable
    );

    @Query("""
        SELECT h FROM BeneficiaryUsageHistory h
        WHERE h.beneficiaire.client.id = :clientId
        AND h.usedAt BETWEEN :startDate AND :endDate
        ORDER BY h.usedAt DESC
    """)
    Page<BeneficiaryUsageHistory> findByClientAndDateRange(
            @Param("clientId") Long clientId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

    @Query("""
        SELECT COUNT(h) FROM BeneficiaryUsageHistory h
        WHERE h.beneficiaire.id = :beneficiaireId
        AND h.status = 'SUCCESS'
    """)
    long countSuccessfulUsagesByBeneficiaire(@Param("beneficiaireId") Long beneficiaireId);

    @Query("""
        SELECT COUNT(h) FROM BeneficiaryUsageHistory h
        WHERE h.beneficiaire.id = :beneficiaireId
    """)
    long countTotalUsagesByBeneficiaire(@Param("beneficiaireId") Long beneficiaireId);

    @Query("""
        SELECT h FROM BeneficiaryUsageHistory h
        WHERE h.beneficiaire.id = :beneficiaireId
        ORDER BY h.usedAt DESC
    """)
    List<BeneficiaryUsageHistory> findRecentUsagesByBeneficiaire(
            @Param("beneficiaireId") Long beneficiaireId,
            Pageable pageable
    );
}
