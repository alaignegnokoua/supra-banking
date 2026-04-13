package com.suprabanking.services.impl;

import com.suprabanking.config.security.CurrentUserService;
import com.suprabanking.models.Beneficiaire;
import com.suprabanking.models.BeneficiaryUsageHistory;
import com.suprabanking.models.Transaction;
import com.suprabanking.repositories.BeneficiaryUsageHistoryRepository;
import com.suprabanking.repositories.BeneficiaireRepository;
import com.suprabanking.repositories.TransactionRepository;
import com.suprabanking.services.BeneficiaryUsageHistoryService;
import com.suprabanking.services.dto.BeneficiaryUsageHistoryDTO;
import com.suprabanking.web.errors.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class BeneficiaryUsageHistoryServiceImpl implements BeneficiaryUsageHistoryService {

    private final BeneficiaryUsageHistoryRepository historyRepository;
    private final BeneficiaireRepository beneficiaireRepository;
    private final TransactionRepository transactionRepository;
    private final CurrentUserService currentUserService;

    @Override
    public void recordUsage(Long beneficiaireId, Long transactionId, Double montant, String typeOperation, String status) {
        log.debug("Recording beneficiary usage: beneficiaire={}, transaction={}, status={}", beneficiaireId, transactionId, status);

        Beneficiaire beneficiaire = beneficiaireRepository.findById(beneficiaireId)
                .orElseThrow(() -> new ResourceNotFoundException("Bénéficiaire introuvable"));

        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction introuvable"));

        BeneficiaryUsageHistory history = new BeneficiaryUsageHistory();
        history.setBeneficiaire(beneficiaire);
        history.setTransaction(transaction);
        history.setMontant(montant);
        history.setTypeOperation(typeOperation);
        history.setStatus(status);
        history.setUsedAt(LocalDateTime.now());
        history.setCreatedAt(LocalDateTime.now());

        historyRepository.save(history);
        log.debug("Beneficiary usage recorded successfully");
    }

    @Override
    public Page<BeneficiaryUsageHistoryDTO> getMyBeneficiaryUsageHistory(Pageable pageable) {
        log.debug("Fetching my beneficiary usage history");
        Long clientId = currentUserService.requireCurrentClientId();

        return historyRepository.findByClient(clientId, pageable)
                .map(this::toDto);
    }

    @Override
    public Page<BeneficiaryUsageHistoryDTO> getMyBeneficiaryUsageHistory(Long beneficiaireId, Pageable pageable) {
        log.debug("Fetching usage history for beneficiary: {}", beneficiaireId);
        Long clientId = currentUserService.requireCurrentClientId();

        // Verify that the beneficiary belongs to the current client
        beneficiaireRepository.findByIdAndClient_Id(beneficiaireId, clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Bénéficiaire introuvable"));

        return historyRepository.findByBeneficiaireAndClient(beneficiaireId, clientId, pageable)
                .map(this::toDto);
    }

    @Override
    public Page<BeneficiaryUsageHistoryDTO> getMyBeneficiaryUsageHistoryByDateRange(
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    ) {
        log.debug("Fetching beneficiary usage history for date range: {} to {}", startDate, endDate);
        Long clientId = currentUserService.requireCurrentClientId();

        return historyRepository.findByClientAndDateRange(clientId, startDate, endDate, pageable)
                .map(this::toDto);
    }

    @Override
    public long getSuccessfulUsageCount(Long beneficiaireId) {
        log.debug("Getting successful usage count for beneficiary: {}", beneficiaireId);
        return historyRepository.countSuccessfulUsagesByBeneficiaire(beneficiaireId);
    }

    private BeneficiaryUsageHistoryDTO toDto(BeneficiaryUsageHistory entity) {
        return new BeneficiaryUsageHistoryDTO(
                entity.getId(),
                entity.getBeneficiaire().getId(),
                entity.getBeneficiaire().getNom(),
                entity.getTransaction().getId(),
                entity.getMontant(),
                entity.getTypeOperation(),
                entity.getStatus(),
                entity.getUsedAt(),
                entity.getCreatedAt()
        );
    }
}
