package com.suprabanking.services.impl;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;

import com.suprabanking.config.security.CurrentUserService;
import com.suprabanking.models.Beneficiaire;
import com.suprabanking.models.Client;
import com.suprabanking.models.Compte;
import com.suprabanking.models.OperationAudit;
import com.suprabanking.models.Transaction;
import com.suprabanking.repositories.BeneficiaireRepository;
import com.suprabanking.repositories.ClientRepository;
import com.suprabanking.repositories.CompteRepository;
import com.suprabanking.repositories.OperationAuditRepository;
import com.suprabanking.repositories.TransactionRepository;
import com.suprabanking.services.NotificationService;
import com.suprabanking.services.TransactionService;
import com.suprabanking.services.dto.TransactionDTO;
import com.suprabanking.services.dto.TransferLimitStatusDTO;
import com.suprabanking.services.dto.TransferRiskAssessmentDTO;
import com.suprabanking.services.dto.VirementExterneRequest;
import com.suprabanking.services.dto.VirementInterneRequest;
import com.suprabanking.services.dto.OperationAuditDTO;
import com.suprabanking.services.mapper.TransactionMapper;
import com.suprabanking.web.errors.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final BeneficiaireRepository beneficiaireRepository;
    private final CompteRepository compteRepository;
    private final OperationAuditRepository operationAuditRepository;
    private final ClientRepository clientRepository;
    private final NotificationService notificationService;
    private final TransactionMapper transactionMapper;
    private final CurrentUserService currentUserService;

    @Value("${app.transfers.max-single-amount:10000}")
    private Double maxSingleTransferAmount;

    @Value("${app.transfers.max-daily-total:15000}")
    private Double maxDailyTransferTotal;

    @Value("${app.transfers.max-daily-count:10}")
    private Integer maxDailyTransferCount;

    @Value("${app.transfers.min-interval-seconds:30}")
    private Integer minTransferIntervalSeconds;

    @Value("${app.transfers.risk-block-threshold:90}")
    private Integer transferRiskBlockThreshold;

    @Value("${app.transfers.risk-block-threshold-internal:95}")
    private Integer transferRiskBlockThresholdInternal;

    @Value("${app.transfers.risk-block-threshold-external:90}")
    private Integer transferRiskBlockThresholdExternal;

    @Value("${app.transfers.risk-block-threshold-internal-sensitive:90}")
    private Integer transferRiskBlockThresholdInternalSensitive;

    @Value("${app.transfers.risk-block-threshold-internal-standard:95}")
    private Integer transferRiskBlockThresholdInternalStandard;

    @Value("${app.transfers.risk-block-threshold-internal-vip:98}")
    private Integer transferRiskBlockThresholdInternalVip;

    @Value("${app.transfers.risk-block-threshold-external-sensitive:85}")
    private Integer transferRiskBlockThresholdExternalSensitive;

    @Value("${app.transfers.risk-block-threshold-external-standard:90}")
    private Integer transferRiskBlockThresholdExternalStandard;

    @Value("${app.transfers.risk-block-threshold-external-vip:95}")
    private Integer transferRiskBlockThresholdExternalVip;

    @Value("${app.transfers.risk-new-beneficiary-window-hours:24}")
    private Integer newBeneficiaryWindowHours;

    @Value("${app.transfers.risk-new-beneficiary-score-external:15}")
    private Integer newBeneficiaryScoreExternal;

    @Value("${app.transfers.risk-unusual-hour-start:22}")
    private Integer unusualHourStart;

    @Value("${app.transfers.risk-unusual-hour-end:6}")
    private Integer unusualHourEnd;

    @Value("${app.transfers.risk-unusual-hour-score-external:10}")
    private Integer unusualHourScoreExternal;

    @Value("${app.transfers.risk-unusual-hour-score-internal:5}")
    private Integer unusualHourScoreInternal;

    @Override
    public TransactionDTO saveTransaction(TransactionDTO dto) {
        log.debug("Request to save Transaction : {}", dto);

        Compte compte = compteRepository.findById(dto.getCompteId())
                .orElseThrow(() -> new ResourceNotFoundException("Compte not found with id=" + dto.getCompteId()));

        Client client;
        if (dto.getClientId() != null) {
            client = clientRepository.findById(dto.getClientId())
                    .orElseThrow(() -> new ResourceNotFoundException("Client not found with id=" + dto.getClientId()));
        } else {
            client = compte.getClient();
        }

        Transaction entity = transactionMapper.toEntity(dto);
        entity.setCompte(compte);
        entity.setClient(client);
        if (entity.getDateTransaction() == null) {
            entity.setDateTransaction(LocalDateTime.now());
        }

        return transactionMapper.toDto(transactionRepository.save(entity));
    }

    @Override
    public TransactionDTO updateTransaction(TransactionDTO dto, Long id) {
        log.debug("Request to update Transaction : {}", dto);
        Transaction entity = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id=" + id));

        Compte compte = compteRepository.findById(dto.getCompteId())
                .orElseThrow(() -> new ResourceNotFoundException("Compte not found with id=" + dto.getCompteId()));

        Client client;
        if (dto.getClientId() != null) {
            client = clientRepository.findById(dto.getClientId())
                    .orElseThrow(() -> new ResourceNotFoundException("Client not found with id=" + dto.getClientId()));
        } else {
            client = compte.getClient();
        }

        entity.setType(dto.getType());
        entity.setMontant(dto.getMontant());
        entity.setDateTransaction(dto.getDateTransaction() != null ? dto.getDateTransaction() : entity.getDateTransaction());
        entity.setDescription(dto.getDescription());
        entity.setCompte(compte);
        entity.setClient(client);

        return transactionMapper.toDto(transactionRepository.save(entity));
    }

    @Override
    public TransactionDTO partialUpdateTransaction(TransactionDTO dto, Long id) {
        log.debug("Request to partial update Transaction : {}", dto);
        return transactionRepository.findById(id)
                .map(existing -> {
                    transactionMapper.partialUpdate(existing, dto);

                    if (dto.getCompteId() != null) {
                        Compte compte = compteRepository.findById(dto.getCompteId())
                                .orElseThrow(() -> new ResourceNotFoundException("Compte not found with id=" + dto.getCompteId()));
                        existing.setCompte(compte);
                        if (dto.getClientId() == null && existing.getClient() == null) {
                            existing.setClient(compte.getClient());
                        }
                    }

                    if (dto.getClientId() != null) {
                        Client client = clientRepository.findById(dto.getClientId())
                                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id=" + dto.getClientId()));
                        existing.setClient(client);
                    }

                    return transactionMapper.toDto(transactionRepository.save(existing));
                })
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id=" + id));
    }

    @Override
    @Transactional
    public void effectuerVirementInterne(VirementInterneRequest request) {
        log.debug("Request to process internal transfer: {}", request);
        Long clientId = currentUserService.requireCurrentClientId();

        if (request.getCompteSourceId().equals(request.getCompteDestinationId())) {
            saveAudit("VIREMENT_INTERNE", "ECHEC", "Comptes identiques", clientId,
                    request.getCompteSourceId(), request.getCompteDestinationId(), null, request.getMontant());
            throw new IllegalArgumentException("Le compte source et le compte destination doivent être différents");
        }

        Compte compteSource = compteRepository.findById(request.getCompteSourceId())
                .orElseThrow(() -> new ResourceNotFoundException("Compte source introuvable"));

        Compte compteDestination = compteRepository.findById(request.getCompteDestinationId())
                .orElseThrow(() -> new ResourceNotFoundException("Compte destination introuvable"));

        if (compteSource.getClient() == null || !clientId.equals(compteSource.getClient().getId())
                || compteDestination.getClient() == null || !clientId.equals(compteDestination.getClient().getId())) {
            saveAudit("VIREMENT_INTERNE", "ECHEC", "Accès refusé à l'un des comptes", clientId,
                    request.getCompteSourceId(), request.getCompteDestinationId(), null, request.getMontant());
            throw new AccessDeniedException("Accès refusé à l'un des comptes");
        }

        if (compteSource.getSolde() == null || compteSource.getSolde() < request.getMontant()) {
            saveAudit("VIREMENT_INTERNE", "ECHEC", "Solde insuffisant", clientId,
                    request.getCompteSourceId(), request.getCompteDestinationId(), null, request.getMontant());
            throw new IllegalArgumentException("Solde insuffisant pour effectuer ce virement");
        }

        validateTransferLimits(clientId, request.getMontant(), "VIREMENT_INTERNE",
            request.getCompteSourceId(), request.getCompteDestinationId(), null);

        compteSource.setSolde(compteSource.getSolde() - request.getMontant());
        compteDestination.setSolde(compteDestination.getSolde() + request.getMontant());

        compteRepository.save(compteSource);
        compteRepository.save(compteDestination);

        String descriptionBase = (request.getDescription() == null || request.getDescription().isBlank())
                ? "Virement interne"
                : request.getDescription().trim();

        LocalDateTime now = LocalDateTime.now();
        Client client = compteSource.getClient();

        Transaction debit = new Transaction();
        debit.setType("virement");
        debit.setMontant(request.getMontant());
        debit.setDateTransaction(now);
        debit.setDescription(descriptionBase + " - débit vers " + compteDestination.getNumeroCompte());
        debit.setClient(client);
        debit.setCompte(compteSource);

        Transaction credit = new Transaction();
        credit.setType("virement");
        credit.setMontant(request.getMontant());
        credit.setDateTransaction(now);
        credit.setDescription(descriptionBase + " - crédit depuis " + compteSource.getNumeroCompte());
        credit.setClient(client);
        credit.setCompte(compteDestination);

        transactionRepository.save(debit);
        transactionRepository.save(credit);

        saveAudit("VIREMENT_INTERNE", "SUCCES", "Virement interne effectué", clientId,
            request.getCompteSourceId(), request.getCompteDestinationId(), null, request.getMontant());

        notificationService.createForClient(clientId, "Virement interne effectué : " + request.getMontant()
            + " de " + compteSource.getNumeroCompte() + " vers " + compteDestination.getNumeroCompte());
    }

    @Override
    @Transactional
    public void effectuerVirementExterne(VirementExterneRequest request) {
        log.debug("Request to process external transfer: {}", request);

        Long clientId = currentUserService.requireCurrentClientId();

        Compte compteSource = compteRepository.findById(request.getCompteSourceId())
                .orElseThrow(() -> new ResourceNotFoundException("Compte source introuvable"));

        if (compteSource.getClient() == null || !clientId.equals(compteSource.getClient().getId())) {
            saveAudit("VIREMENT_EXTERNE", "ECHEC", "Accès refusé au compte source", clientId,
                    request.getCompteSourceId(), null, request.getBeneficiaireId(), request.getMontant());
            throw new AccessDeniedException("Accès refusé au compte source");
        }

        Beneficiaire beneficiaire = beneficiaireRepository.findByIdAndClient_Id(request.getBeneficiaireId(), clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Bénéficiaire introuvable"));

        if (compteSource.getSolde() == null || compteSource.getSolde() < request.getMontant()) {
            saveAudit("VIREMENT_EXTERNE", "ECHEC", "Solde insuffisant", clientId,
                    request.getCompteSourceId(), null, request.getBeneficiaireId(), request.getMontant());
            throw new IllegalArgumentException("Solde insuffisant pour effectuer ce virement externe");
        }

        validateTransferLimits(clientId, request.getMontant(), "VIREMENT_EXTERNE",
            request.getCompteSourceId(), null, request.getBeneficiaireId());

        compteSource.setSolde(compteSource.getSolde() - request.getMontant());
        compteRepository.save(compteSource);

        String descriptionBase = (request.getDescription() == null || request.getDescription().isBlank())
                ? "Virement externe"
                : request.getDescription().trim();

        String destination = beneficiaire.getIban() != null && !beneficiaire.getIban().isBlank()
                ? beneficiaire.getIban()
                : beneficiaire.getRib();

        Transaction debit = new Transaction();
        debit.setType("virement_externe");
        debit.setMontant(request.getMontant());
        debit.setDateTransaction(LocalDateTime.now());
        debit.setDescription(descriptionBase + " - bénéficiaire " + beneficiaire.getNom() + " (" + destination + ")");
        debit.setClient(compteSource.getClient());
        debit.setCompte(compteSource);

        transactionRepository.save(debit);

        saveAudit("VIREMENT_EXTERNE", "SUCCES", "Virement externe effectué", clientId,
            request.getCompteSourceId(), null, request.getBeneficiaireId(), request.getMontant());

        notificationService.createForClient(clientId, "Virement externe effectué : " + request.getMontant()
            + " vers " + beneficiaire.getNom());
    }

    @Override
    public Page<TransactionDTO> findAllTransactions(Pageable pageable) {
        log.debug("Request to get all Transactions");

        if (currentUserService.isCurrentUserClient()) {
            Long clientId = currentUserService.requireCurrentClientId();
            return transactionRepository.findByClient_Id(clientId, pageable).map(transactionMapper::toDto);
        }

        return transactionRepository.findAll(pageable).map(transactionMapper::toDto);
    }

    @Override
    public TransferLimitStatusDTO getMyTransferLimits() {
        Long clientId = currentUserService.requireCurrentClientId();
        Double todayOutgoingTotal = getTodayOutgoingTotal(clientId);
        Long todayOutgoingCountLong = getTodayOutgoingCount(clientId);
        int todayOutgoingCount = todayOutgoingCountLong == null ? 0 : todayOutgoingCountLong.intValue();
        int remainingCooldownSeconds = getRemainingCooldownSeconds(clientId);

        double effectiveDailyMax = maxDailyTransferTotal == null ? 0.0 : maxDailyTransferTotal;
        double remainingDailyAmount = Math.max(0.0, effectiveDailyMax - todayOutgoingTotal);
        int effectiveDailyCountMax = maxDailyTransferCount == null ? 0 : maxDailyTransferCount;
        int remainingDailyCount = Math.max(0, effectiveDailyCountMax - todayOutgoingCount);

        return new TransferLimitStatusDTO(
                maxSingleTransferAmount,
                maxDailyTransferTotal,
            maxDailyTransferCount,
                minTransferIntervalSeconds,
                todayOutgoingTotal,
            remainingDailyAmount,
            todayOutgoingCount,
                remainingDailyCount,
                remainingCooldownSeconds
        );
    }

    @Override
    public TransferRiskAssessmentDTO getMyTransferRiskPreview(Double montant, String operationType, Long beneficiaireId) {
        Long clientId = currentUserService.requireCurrentClientId();
        String normalizedOperationType = normalizeOperationType(operationType);
        return assessTransferRisk(clientId, montant, normalizedOperationType, beneficiaireId);
    }

    @Override
    public Page<TransactionDTO> findMyTransactionsByCompte(
            Long compteId,
            String type,
            LocalDateTime dateFrom,
            LocalDateTime dateTo,
            Double montantMin,
            Double montantMax,
            Pageable pageable
    ) {
        log.debug("Request to get current client transactions by compte {}, filters type={}, from={}, to={}, min={}, max={}",
            compteId, type, dateFrom, dateTo, montantMin, montantMax);

        Long clientId = currentUserService.requireCurrentClientId();

        Compte compte = compteRepository.findById(compteId)
            .orElseThrow(() -> new ResourceNotFoundException("Compte not found with id=" + compteId));

        if (compte.getClient() == null || !clientId.equals(compte.getClient().getId())) {
            throw new AccessDeniedException("Accès refusé à ce compte");
        }

        return transactionRepository.findMyTransactionsByCompteWithFilters(
                clientId,
                compteId,
                type,
                dateFrom,
                dateTo,
                montantMin,
                montantMax,
                pageable
        ).map(transactionMapper::toDto);
    }

    @Override
    public Optional<TransactionDTO> findOne(Long id) {
        log.debug("Request to get Transaction : {}", id);
        if (currentUserService.isCurrentUserClient()) {
            Long clientId = currentUserService.requireCurrentClientId();
            return transactionRepository.findByIdAndClient_Id(id, clientId)
                    .map(transactionMapper::toDto);
        }

        return transactionRepository.findById(id).map(transactionMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete Transaction : {}", id);
        transactionRepository.deleteById(id);
    }

    private void saveAudit(
            String operationType,
            String status,
            String message,
            Long clientId,
            Long compteSourceId,
            Long compteDestinationId,
            Long beneficiaireId,
            Double montant
    ) {
        saveAudit(operationType, status, message, clientId, compteSourceId, compteDestinationId, beneficiaireId, montant, null);
    }

    private void saveAudit(
            String operationType,
            String status,
            String message,
            Long clientId,
            Long compteSourceId,
            Long compteDestinationId,
            Long beneficiaireId,
            Double montant,
            TransferRiskAssessmentDTO risk
    ) {
        OperationAudit audit = new OperationAudit();
        audit.setOperationType(operationType);
        audit.setStatus(status);
        audit.setMessage(message);
        audit.setCreatedAt(LocalDateTime.now());
        audit.setClientId(clientId);
        audit.setCompteSourceId(compteSourceId);
        audit.setCompteDestinationId(compteDestinationId);
        audit.setBeneficiaireId(beneficiaireId);
        audit.setMontant(montant);
        
        if (risk != null) {
            audit.setRiskScore(risk.getScore());
            audit.setRiskLevel(risk.getLevel());
            audit.setRiskBlocked(risk.getBlocked());
            
            // Store risk details as a map
            java.util.Map<String, Object> riskDetails = new java.util.HashMap<>();
            riskDetails.put("operationType", risk.getOperationType());
            riskDetails.put("riskProfile", risk.getRiskProfile());
            riskDetails.put("blockThreshold", risk.getBlockThreshold());
            riskDetails.put("amountRatio", risk.getAmountRatio());
            riskDetails.put("dailyAmountRatio", risk.getDailyAmountRatio());
            riskDetails.put("dailyCountRatio", risk.getDailyCountRatio());
            riskDetails.put("amountScore", risk.getAmountScore());
            riskDetails.put("dailyAmountScore", risk.getDailyAmountScore());
            riskDetails.put("dailyCountScore", risk.getDailyCountScore());
            riskDetails.put("newBeneficiary", risk.getNewBeneficiary());
            riskDetails.put("newBeneficiaryScore", risk.getNewBeneficiaryScore());
            riskDetails.put("unusualHour", risk.getUnusualHour());
            riskDetails.put("unusualHourScore", risk.getUnusualHourScore());
            audit.setRiskDetails(riskDetails);
        }
        
        operationAuditRepository.save(audit);
    }

    private void validateTransferLimits(
            Long clientId,
            Double montant,
            String operationType,
            Long compteSourceId,
            Long compteDestinationId,
            Long beneficiaireId
    ) {
        if (montant == null || montant <= 0) {
            return;
        }

        if (maxSingleTransferAmount != null && montant > maxSingleTransferAmount) {
            saveAudit(operationType, "ECHEC", "Plafond unitaire dépassé", clientId,
                    compteSourceId, compteDestinationId, beneficiaireId, montant);
            throw new IllegalArgumentException("Montant supérieur au plafond unitaire autorisé");
        }

        TransferRiskAssessmentDTO risk = assessTransferRisk(clientId, montant, operationType, beneficiaireId);
        if (Boolean.TRUE.equals(risk.getBlocked())) {
            saveAudit(operationType, "ECHEC", "Risque élevé détecté: score=" + risk.getScore(), clientId,
                compteSourceId, compteDestinationId, beneficiaireId, montant, risk);
            notificationService.createForClient(
                    clientId,
                    "ALERTE FRAUDE - Virement " + normalizeOperationType(operationType)
                            + " bloqué (score=" + risk.getScore() + ", montant=" + montant + ")"
            );
            throw new IllegalArgumentException(risk.getMessage());
        }

        Double alreadyTransferred = getTodayOutgoingTotal(clientId);
        double totalAfterTransfer = (alreadyTransferred == null ? 0.0 : alreadyTransferred) + montant;

        if (maxDailyTransferTotal != null && totalAfterTransfer > maxDailyTransferTotal) {
            saveAudit(operationType, "ECHEC", "Plafond journalier dépassé", clientId,
                    compteSourceId, compteDestinationId, beneficiaireId, montant);
            throw new IllegalArgumentException("Plafond journalier de virement dépassé");
        }

        Long todayTransferCount = getTodayOutgoingCount(clientId);
        long countAfterTransfer = (todayTransferCount == null ? 0L : todayTransferCount) + 1L;

        if (maxDailyTransferCount != null && countAfterTransfer > maxDailyTransferCount) {
            saveAudit(operationType, "ECHEC", "Nombre de virements journalier dépassé", clientId,
                    compteSourceId, compteDestinationId, beneficiaireId, montant);
            throw new IllegalArgumentException("Nombre maximal de virements journaliers atteint");
        }

        int remainingCooldownSeconds = getRemainingCooldownSeconds(clientId);
        if (remainingCooldownSeconds > 0) {
            saveAudit(operationType, "ECHEC", "Délai minimal entre virements non respecté", clientId,
                compteSourceId, compteDestinationId, beneficiaireId, montant);
            throw new IllegalArgumentException("Veuillez attendre " + remainingCooldownSeconds
                + " secondes avant d'effectuer un nouveau virement");
        }
    }

    private Double getTodayOutgoingTotal(Long clientId) {
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusNanos(1);
        Double total = transactionRepository.sumDailyOutgoingTransfers(clientId, startOfDay, endOfDay);
        return total == null ? 0.0 : total;
    }

    private Long getTodayOutgoingCount(Long clientId) {
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusNanos(1);
        Long count = transactionRepository.countDailyOutgoingTransfers(clientId, startOfDay, endOfDay);
        return count == null ? 0L : count;
    }

    private int getRemainingCooldownSeconds(Long clientId) {
        if (minTransferIntervalSeconds == null || minTransferIntervalSeconds <= 0) {
            return 0;
        }

        LocalDateTime lastOutgoingTransferAt = transactionRepository.findLastOutgoingTransferAt(clientId);
        if (lastOutgoingTransferAt == null) {
            return 0;
        }

        long elapsedSeconds = ChronoUnit.SECONDS.between(lastOutgoingTransferAt, LocalDateTime.now());
        long remainingSeconds = (long) minTransferIntervalSeconds - elapsedSeconds;
        return (int) Math.max(0L, remainingSeconds);
    }

    private TransferRiskAssessmentDTO assessTransferRisk(Long clientId, Double montant, String operationType, Long beneficiaireId) {
        String normalizedOperationType = normalizeOperationType(operationType);
        boolean isExternal = isExternalOperation(normalizedOperationType);
        String riskProfile = clientRepository.findById(clientId)
                .map(Client::getRiskProfile)
                .map(this::normalizeRiskProfile)
                .orElse("STANDARD");

        int threshold = resolveDynamicBlockThreshold(isExternal, riskProfile);
        boolean newBeneficiary = isExternal && isNewBeneficiary(clientId, beneficiaireId);
        int beneficiaryScore = newBeneficiary
                ? (newBeneficiaryScoreExternal == null ? 15 : newBeneficiaryScoreExternal)
                : 0;
        boolean unusualHour = isUnusualHour(LocalDateTime.now().toLocalTime());
        int unusualHourScore = unusualHour
            ? (isExternal
                ? (unusualHourScoreExternal == null ? 10 : unusualHourScoreExternal)
                : (unusualHourScoreInternal == null ? 5 : unusualHourScoreInternal))
            : 0;

        if (montant == null || montant <= 0) {
            return new TransferRiskAssessmentDTO(
                0,
                "FAIBLE",
                false,
                "Risque faible",
                normalizedOperationType,
                riskProfile,
                threshold,
                0.0,
                0.0,
                0.0,
                0,
                0,
                0,
                newBeneficiary,
                beneficiaryScore,
                unusualHour,
                unusualHourScore
            );
        }

        double amountRatio = ratio(montant, maxSingleTransferAmount);
        double dailyAmountRatio = ratio(getTodayOutgoingTotal(clientId) + montant, maxDailyTransferTotal);
        double dailyCountRatio = ratio((double) (getTodayOutgoingCount(clientId) + 1L),
                maxDailyTransferCount == null ? null : maxDailyTransferCount.doubleValue());

        double amountWeight = isExternal ? 0.6 : 0.4;
        double dailyAmountWeight = isExternal ? 0.25 : 0.4;
        double dailyCountWeight = isExternal ? 0.15 : 0.2;

        int amountScore = (int) Math.round(amountRatio * amountWeight * 100.0);
        int dailyAmountScore = (int) Math.round(dailyAmountRatio * dailyAmountWeight * 100.0);
        int dailyCountScore = (int) Math.round(dailyCountRatio * dailyCountWeight * 100.0);

        int score = amountScore + dailyAmountScore + dailyCountScore + beneficiaryScore + unusualHourScore;
        if (score > 100) {
            score = 100;
        }

        String level = score >= 70 ? "ELEVE" : (score >= 40 ? "MOYEN" : "FAIBLE");
        boolean blocked = score >= threshold;

        String message = blocked
                ? "Risque de fraude élevé détecté (score=" + score + ")"
                : "Risque " + level.toLowerCase() + " (score=" + score + ")";

        return new TransferRiskAssessmentDTO(
            score,
            level,
            blocked,
            message,
            normalizedOperationType,
            riskProfile,
            threshold,
            amountRatio,
            dailyAmountRatio,
            dailyCountRatio,
            amountScore,
            dailyAmountScore,
            dailyCountScore,
            newBeneficiary,
            beneficiaryScore,
            unusualHour,
            unusualHourScore
        );
    }

    private boolean isUnusualHour(LocalTime time) {
        int start = unusualHourStart == null ? 22 : unusualHourStart;
        int end = unusualHourEnd == null ? 6 : unusualHourEnd;

        if (start < 0 || start > 23 || end < 0 || end > 23) {
            start = 22;
            end = 6;
        }

        int hour = time.getHour();
        if (start == end) {
            return true;
        }

        if (start < end) {
            return hour >= start && hour <= end;
        }

        return hour >= start || hour <= end;
    }

    private boolean isNewBeneficiary(Long clientId, Long beneficiaireId) {
        if (beneficiaireId == null) {
            return false;
        }

        return beneficiaireRepository.findByIdAndClient_Id(beneficiaireId, clientId)
                .map(Beneficiaire::getCreatedAt)
                .map(createdAt -> {
                    if (createdAt == null) {
                        return false;
                    }
                    int windowHours = newBeneficiaryWindowHours == null || newBeneficiaryWindowHours <= 0
                            ? 24
                            : newBeneficiaryWindowHours;
                    return createdAt.isAfter(LocalDateTime.now().minusHours(windowHours));
                })
                .orElse(false);
    }

    private String normalizeOperationType(String operationType) {
        if (operationType == null || operationType.isBlank()) {
            return "EXTERNE";
        }

        String raw = operationType.trim().toUpperCase();
        if (raw.contains("INTERNE")) {
            return "INTERNE";
        }

        return "EXTERNE";
    }

    private boolean isExternalOperation(String normalizedOperationType) {
        return "EXTERNE".equals(normalizedOperationType) || "VIREMENT_EXTERNE".equals(normalizedOperationType);
    }

    private String normalizeRiskProfile(String riskProfile) {
        if (riskProfile == null || riskProfile.isBlank()) {
            return "STANDARD";
        }

        String normalized = riskProfile.trim().toUpperCase();
        if ("SENSIBLE".equals(normalized) || "VIP".equals(normalized)) {
            return normalized;
        }

        return "STANDARD";
    }

    private int resolveDynamicBlockThreshold(boolean isExternal, String riskProfile) {
        int defaultThreshold = transferRiskBlockThreshold == null ? 90 : transferRiskBlockThreshold;
        int defaultExternal = transferRiskBlockThresholdExternal == null ? defaultThreshold : transferRiskBlockThresholdExternal;
        int defaultInternal = transferRiskBlockThresholdInternal == null ? defaultThreshold : transferRiskBlockThresholdInternal;

        if (isExternal) {
            return switch (riskProfile) {
                case "SENSIBLE" -> transferRiskBlockThresholdExternalSensitive == null
                        ? defaultExternal
                        : transferRiskBlockThresholdExternalSensitive;
                case "VIP" -> transferRiskBlockThresholdExternalVip == null
                        ? defaultExternal
                        : transferRiskBlockThresholdExternalVip;
                default -> transferRiskBlockThresholdExternalStandard == null
                        ? defaultExternal
                        : transferRiskBlockThresholdExternalStandard;
            };
        }

        return switch (riskProfile) {
            case "SENSIBLE" -> transferRiskBlockThresholdInternalSensitive == null
                    ? defaultInternal
                    : transferRiskBlockThresholdInternalSensitive;
            case "VIP" -> transferRiskBlockThresholdInternalVip == null
                    ? defaultInternal
                    : transferRiskBlockThresholdInternalVip;
            default -> transferRiskBlockThresholdInternalStandard == null
                    ? defaultInternal
                    : transferRiskBlockThresholdInternalStandard;
        };
    }

    private double ratio(Double value, Double max) {
        if (value == null || value <= 0 || max == null || max <= 0) {
            return 0.0;
        }
        return Math.min(1.0, value / max);
    }

    @Override
    public Page<OperationAuditDTO> getMyTransferAuditHistory(Pageable pageable) {
        Long clientId = currentUserService.requireCurrentClientId();
        return operationAuditRepository.findByClientIdOrderByCreatedAtDesc(clientId, pageable)
                .map(audit -> new OperationAuditDTO(
                        audit.getId(),
                        audit.getOperationType(),
                        audit.getStatus(),
                        audit.getMessage(),
                        audit.getCreatedAt(),
                        audit.getClientId(),
                        audit.getCompteSourceId(),
                        audit.getCompteDestinationId(),
                        audit.getBeneficiaireId(),
                        audit.getMontant(),
                        audit.getRiskScore(),
                        audit.getRiskLevel(),
                        audit.getRiskBlocked(),
                        audit.getRiskDetails()
                ));
    }
}
