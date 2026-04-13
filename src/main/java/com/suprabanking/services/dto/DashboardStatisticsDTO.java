package com.suprabanking.services.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardStatisticsDTO implements Serializable {

    // Statistiques globales
    private long totalClients;
    private long activeClientsToday;
    private long totalAccounts;
    private long totalTransactions;
    private Double totalTransactionAmount;

    // Statistiques des transferts
    private long transfersToday;
    private Double transfersAmountToday;
    private long internalTransfersToday;
    private long externalTransfersToday;
    private Double averageTransferAmount;

    // Statistiques de fraude/risque
    private long blockedTransfersToday;
    private long highRiskTransactionsToday;
    private long mediumRiskTransactionsToday;
    private Double fraudBlockageRate;

    // Statistiques des bénéficiaires
    private long totalBeneficiaries;
    private long newBeneficiariesThisMonth;
    private long activeBeneficiaries;

    // Statistiques des notifications
    private long unreadNotifications;
    private long notificationsToday;

    // Santé du système
    private Double databaseHealthPercentage;
    private LocalDateTime lastUpdateTime;
    private Integer averageResponseTimeMs;

    // Top données
    private String topOperationType;
    private String topRiskLevel;
    private Double topClientTransactionAmount;
}
