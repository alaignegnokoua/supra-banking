package com.suprabanking.services.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlyReportDTO {

    private int year;
    private int month;

    private long totalTransactions;
    private double totalTransactionsAmount;

    private long totalInternalTransfers;
    private long totalExternalTransfers;
    private long blockedTransfers;

    private long totalAuditEntries;
    private long successfulAuditEntries;
    private long failedAuditEntries;

    private String summary;
}
