package com.suprabanking.services;

import com.suprabanking.services.dto.BeneficiaryUsageHistoryDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface BeneficiaryUsageHistoryService {

    void recordUsage(Long beneficiaireId, Long transactionId, Double montant, String typeOperation, String status);

    Page<BeneficiaryUsageHistoryDTO> getMyBeneficiaryUsageHistory(Pageable pageable);

    Page<BeneficiaryUsageHistoryDTO> getMyBeneficiaryUsageHistory(Long beneficiaireId, Pageable pageable);

    Page<BeneficiaryUsageHistoryDTO> getMyBeneficiaryUsageHistoryByDateRange(
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    );

    long getSuccessfulUsageCount(Long beneficiaireId);
}
