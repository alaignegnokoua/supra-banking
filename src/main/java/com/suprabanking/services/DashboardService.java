package com.suprabanking.services;

import com.suprabanking.services.dto.DashboardStatisticsDTO;
import com.suprabanking.services.dto.MonthlyReportDTO;

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

    MonthlyReportDTO getMonthlyReport(int year, int month);

    String exportMonthlyTransactionsCsv(int year, int month);

    String exportMonthlyAuditsCsv(int year, int month);
}
