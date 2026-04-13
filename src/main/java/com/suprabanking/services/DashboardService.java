package com.suprabanking.services;

import com.suprabanking.services.dto.DashboardStatisticsDTO;

public interface DashboardService {

    DashboardStatisticsDTO getDashboardStatistics();

    long getTotalClients();

    long getActiveClientsToday();

    long getTotalTransactions();

    long getTransfersToday();

    long getBlockedTransfersToday();

    long getHighRiskTransactionsToday();

    Double getTotalTransactionAmount();

    Double getTransfersAmountToday();

    long getTotalBeneficiaries();

    long getActiveBeneficiaries();
}
