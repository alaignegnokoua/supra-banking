package com.suprabanking.services.impl;

import com.suprabanking.models.Transaction;
import com.suprabanking.repositories.BeneficiaireRepository;
import com.suprabanking.repositories.ClientRepository;
import com.suprabanking.repositories.NotificationRepository;
import com.suprabanking.repositories.OperationAuditRepository;
import com.suprabanking.repositories.TransactionRepository;
import com.suprabanking.services.DashboardService;
import com.suprabanking.services.dto.DashboardStatisticsDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;

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
        return notificationRepository.countByReadFalse();
    }

    private long getNotificationsTodayCount() {
        LocalDateTime today = LocalDateTime.now().with(LocalTime.MIN);
        LocalDateTime endOfToday = LocalDateTime.now().with(LocalTime.MAX);
        return notificationRepository.countByCreatedAtBetween(today, endOfToday);
    }
}
