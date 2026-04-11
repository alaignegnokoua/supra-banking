package com.suprabanking.services;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.suprabanking.services.dto.TransactionDTO;
import com.suprabanking.services.dto.TransferLimitStatusDTO;
import com.suprabanking.services.dto.TransferRiskAssessmentDTO;
import com.suprabanking.services.dto.VirementExterneRequest;
import com.suprabanking.services.dto.VirementInterneRequest;
import com.suprabanking.services.dto.OperationAuditDTO;

public interface TransactionService {

    TransactionDTO saveTransaction(TransactionDTO transactionDTO);

    TransactionDTO updateTransaction(TransactionDTO transactionDTO, Long id);

    TransactionDTO partialUpdateTransaction(TransactionDTO transactionDTO, Long id);

    void effectuerVirementInterne(VirementInterneRequest request);

    void effectuerVirementExterne(VirementExterneRequest request);

    TransferLimitStatusDTO getMyTransferLimits();

    TransferRiskAssessmentDTO getMyTransferRiskPreview(Double montant, String operationType);

    Page<OperationAuditDTO> getMyTransferAuditHistory(Pageable pageable);

    Page<TransactionDTO> findAllTransactions(Pageable pageable);

    Page<TransactionDTO> findMyTransactionsByCompte(
            Long compteId,
            String type,
            LocalDateTime dateFrom,
            LocalDateTime dateTo,
            Double montantMin,
            Double montantMax,
            Pageable pageable
    );

    Optional<TransactionDTO> findOne(Long id);

    void delete(Long id);
}
