package com.suprabanking.services.impl;

import com.suprabanking.models.Transaction;
import com.suprabanking.models.OperationAudit;
import com.suprabanking.repositories.BeneficiaireRepository;
import com.suprabanking.repositories.ClientRepository;
import com.suprabanking.repositories.NotificationRepository;
import com.suprabanking.repositories.OperationAuditRepository;
import com.suprabanking.repositories.TransactionRepository;
import com.suprabanking.services.DashboardService;
import com.suprabanking.services.dto.DashboardStatisticsDTO;
import com.suprabanking.services.dto.MonthlyReportDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardServiceImpl implements DashboardService {

    private final ClientRepository clientRepository;
    private final TransactionRepository transactionRepository;
    private final BeneficiaireRepository beneficiaireRepository;
    private final NotificationRepository notificationRepository;
    private final OperationAuditRepository operationAuditRepository;

    @Override
    public DashboardStatisticsDTO getDashboardStatistics() {
        log.debug("Fetching dashboard statistics");

        LocalDateTime today = LocalDateTime.now().with(LocalTime.MIN);
        LocalDateTime endOfToday = LocalDateTime.now().with(LocalTime.MAX);

        return DashboardStatisticsDTO.builder()
                .totalClients(getTotalClients())
                .activeClientsToday(getActiveClientsToday())
                .totalAccounts(getTotalAccountsCount())
                .totalTransactions(getTotalTransactions())
                .totalTransactionAmount(getTotalTransactionAmount())

                .transfersToday(getTransfersToday())
                .transfersAmountToday(getTransfersAmountToday())
                .internalTransfersToday(getInternalTransfersToday())
                .externalTransfersToday(getExternalTransfersToday())
                .averageTransferAmount(getAverageTransferAmount())

                .blockedTransfersToday(getBlockedTransfersToday())
                .highRiskTransactionsToday(getHighRiskTransactionsToday())
                .mediumRiskTransactionsToday(getMediumRiskTransactionsToday())
                .fraudBlockageRate(calculateFraudBlockageRate())

                .totalBeneficiaries(getTotalBeneficiaries())
                .newBeneficiariesThisMonth(getNewBeneficiariesThisMonth())
                .activeBeneficiaries(getActiveBeneficiaries())

                .unreadNotifications(getUnreadNotificationsCount())
                .notificationsToday(getNotificationsTodayCount())

                .databaseHealthPercentage(100.0) // Placeholder
                .lastUpdateTime(LocalDateTime.now())
                .averageResponseTimeMs(50) // Placeholder

                .build();
    }

    @Override
    public long getTotalClients() {
        return clientRepository.count();
    }

    @Override
    public long getActiveClientsToday() {
        // Clients who had at least one transaction today
        LocalDateTime today = LocalDateTime.now().with(LocalTime.MIN);
        LocalDateTime endOfToday = LocalDateTime.now().with(LocalTime.MAX);
        return transactionRepository.countDistinctClientsByDateRange(today, endOfToday);
    }

    private long getTotalAccountsCount() {
        // This would need a CompteRepository method
        return 0; // Placeholder - implement with actual repository
    }

    @Override
    public long getTotalTransactions() {
        return transactionRepository.count();
    }

    @Override
    public long getTransfersToday() {
        LocalDateTime today = LocalDateTime.now().with(LocalTime.MIN);
        LocalDateTime endOfToday = LocalDateTime.now().with(LocalTime.MAX);
        return transactionRepository.countByTypeAndDateRange("virement", today, endOfToday)
                + transactionRepository.countByTypeAndDateRange("virement_externe", today, endOfToday);
    }

    private long getInternalTransfersToday() {
        LocalDateTime today = LocalDateTime.now().with(LocalTime.MIN);
        LocalDateTime endOfToday = LocalDateTime.now().with(LocalTime.MAX);
        return transactionRepository.countByTypeAndDateRange("virement", today, endOfToday);
    }

    private long getExternalTransfersToday() {
        LocalDateTime today = LocalDateTime.now().with(LocalTime.MIN);
        LocalDateTime endOfToday = LocalDateTime.now().with(LocalTime.MAX);
        return transactionRepository.countByTypeAndDateRange("virement_externe", today, endOfToday);
    }

    @Override
    public long getBlockedTransfersToday() {
        LocalDateTime today = LocalDateTime.now().with(LocalTime.MIN);
        LocalDateTime endOfToday = LocalDateTime.now().with(LocalTime.MAX);
        return operationAuditRepository.countByStatusAndDateRange("BLOQUEÉ", today, endOfToday);
    }

    @Override
    public long getHighRiskTransactionsToday() {
        // This would need operation audit with risk level
        return 0; // Placeholder
    }

    private long getMediumRiskTransactionsToday() {
        // This would need operation audit with risk level
        return 0; // Placeholder
    }

    @Override
    public Double getTotalTransactionAmount() {
        Double total = transactionRepository.sumAllTransactionAmounts();
        return total != null ? total : 0.0;
    }

    @Override
    public Double getTransfersAmountToday() {
        LocalDateTime today = LocalDateTime.now().with(LocalTime.MIN);
        LocalDateTime endOfToday = LocalDateTime.now().with(LocalTime.MAX);
        
        Double internalAmount = transactionRepository.sumTransactionsByTypeAndDateRange("virement", today, endOfToday);
        Double externalAmount = transactionRepository.sumTransactionsByTypeAndDateRange("virement_externe", today, endOfToday);
        
        return (internalAmount != null ? internalAmount : 0.0) + (externalAmount != null ? externalAmount : 0.0);
    }

    private Double getAverageTransferAmount() {
        Double avg = transactionRepository.averageTransactionAmount();
        return avg != null ? avg : 0.0;
    }

    private Double calculateFraudBlockageRate() {
        long total = getTransfersToday();
        long blocked = getBlockedTransfersToday();
        return total > 0 ? (double) blocked / total * 100 : 0.0;
    }

    @Override
    public long getTotalBeneficiaries() {
        return beneficiaireRepository.count();
    }

    private long getNewBeneficiariesThisMonth() {
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).with(LocalTime.MIN);
        LocalDateTime now = LocalDateTime.now();
        return beneficiaireRepository.countByCreatedAtBetween(startOfMonth, now);
    }

    @Override
    public long getActiveBeneficiaries() {
        // Beneficiaries used in the last 30 days
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        return beneficiaireRepository.countByLastUsedAtAfter(thirtyDaysAgo);
    }

    private long getUnreadNotificationsCount() {
        return notificationRepository.countByStatut("NON_LU");
    }

    private long getNotificationsTodayCount() {
        LocalDateTime today = LocalDateTime.now().with(LocalTime.MIN);
        LocalDateTime endOfToday = LocalDateTime.now().with(LocalTime.MAX);
        return notificationRepository.countByCreatedAtBetween(today, endOfToday);
    }

    @Override
    public MonthlyReportDTO getMonthlyReport(int year, int month) {
        YearMonth ym = YearMonth.of(year, month);
        LocalDateTime start = ym.atDay(1).atStartOfDay();
        LocalDateTime end = ym.atEndOfMonth().atTime(LocalTime.MAX);

        List<Transaction> transactions = transactionRepository.findByDateTransactionBetweenOrderByDateTransactionDesc(start, end);
        List<OperationAudit> audits = operationAuditRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(start, end);

        long totalTransactions = transactions.size();
        double totalAmount = transactions.stream().mapToDouble(t -> t.getMontant() == null ? 0.0 : t.getMontant()).sum();
        long internalTransfers = transactions.stream().filter(t -> "virement".equalsIgnoreCase(t.getType())).count();
        long externalTransfers = transactions.stream().filter(t -> "virement_externe".equalsIgnoreCase(t.getType())).count();

        long blocked = audits.stream().filter(a -> a.getRiskBlocked() != null && a.getRiskBlocked()).count();
        long auditSuccess = audits.stream().filter(a -> "SUCCES".equalsIgnoreCase(a.getStatus())).count();
        long auditFailure = audits.stream().filter(a -> "ECHEC".equalsIgnoreCase(a.getStatus())).count();

        return MonthlyReportDTO.builder()
                .year(year)
                .month(month)
                .totalTransactions(totalTransactions)
                .totalTransactionsAmount(totalAmount)
                .totalInternalTransfers(internalTransfers)
                .totalExternalTransfers(externalTransfers)
                .blockedTransfers(blocked)
                .totalAuditEntries(audits.size())
                .successfulAuditEntries(auditSuccess)
                .failedAuditEntries(auditFailure)
                .summary("Rapport mensuel généré pour " + ym)
                .build();
    }

    @Override
    public String exportMonthlyTransactionsCsv(int year, int month) {
        YearMonth ym = YearMonth.of(year, month);
        LocalDateTime start = ym.atDay(1).atStartOfDay();
        LocalDateTime end = ym.atEndOfMonth().atTime(LocalTime.MAX);

        List<Transaction> transactions = transactionRepository.findByDateTransactionBetweenOrderByDateTransactionDesc(start, end);
        StringBuilder csv = new StringBuilder();
        csv.append("id,date,type,montant,clientId,compteId,beneficiaireId,description\n");

        for (Transaction t : transactions) {
            csv.append(t.getId()).append(',')
                    .append(t.getDateTransaction()).append(',')
                    .append(escapeCsv(t.getType())).append(',')
                    .append(t.getMontant() == null ? 0.0 : t.getMontant()).append(',')
                    .append(t.getClient() != null ? t.getClient().getId() : "").append(',')
                    .append(t.getCompte() != null ? t.getCompte().getId() : "").append(',')
                    .append(t.getBeneficiaireId() == null ? "" : t.getBeneficiaireId()).append(',')
                    .append(escapeCsv(t.getDescription()))
                    .append('\n');
        }

        return csv.toString();
    }

    @Override
    public String exportMonthlyAuditsCsv(int year, int month) {
        YearMonth ym = YearMonth.of(year, month);
        LocalDateTime start = ym.atDay(1).atStartOfDay();
        LocalDateTime end = ym.atEndOfMonth().atTime(LocalTime.MAX);

        List<OperationAudit> audits = operationAuditRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(start, end);
        StringBuilder csv = new StringBuilder();
        csv.append("id,createdAt,operationType,status,clientId,compteSourceId,compteDestinationId,beneficiaireId,montant,riskScore,riskLevel,riskBlocked,message\n");

        for (OperationAudit a : audits) {
            csv.append(a.getId()).append(',')
                    .append(a.getCreatedAt()).append(',')
                    .append(escapeCsv(a.getOperationType())).append(',')
                    .append(escapeCsv(a.getStatus())).append(',')
                    .append(a.getClientId() == null ? "" : a.getClientId()).append(',')
                    .append(a.getCompteSourceId() == null ? "" : a.getCompteSourceId()).append(',')
                    .append(a.getCompteDestinationId() == null ? "" : a.getCompteDestinationId()).append(',')
                    .append(a.getBeneficiaireId() == null ? "" : a.getBeneficiaireId()).append(',')
                    .append(a.getMontant() == null ? "" : a.getMontant()).append(',')
                    .append(a.getRiskScore() == null ? "" : a.getRiskScore()).append(',')
                    .append(escapeCsv(a.getRiskLevel())).append(',')
                    .append(a.getRiskBlocked() == null ? "" : a.getRiskBlocked()).append(',')
                    .append(escapeCsv(a.getMessage()))
                    .append('\n');
        }

        return csv.toString();
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        String escaped = value.replace("\"", "\"\"");
        if (escaped.contains(",") || escaped.contains("\n") || escaped.contains("\r")) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }
}
