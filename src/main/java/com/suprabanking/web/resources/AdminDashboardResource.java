package com.suprabanking.web.resources;

import com.suprabanking.services.DashboardService;
import com.suprabanking.services.dto.DashboardStatisticsDTO;
import com.suprabanking.services.dto.MonthlyReportDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/admin/dashboard")
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardResource {

    private final DashboardService dashboardService;

    @GetMapping("/statistics")
    public ResponseEntity<DashboardStatisticsDTO> getDashboardStatistics() {
        log.debug("REST request to get dashboard statistics");
        DashboardStatisticsDTO stats = dashboardService.getDashboardStatistics();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/clients/active-today")
    public ResponseEntity<Long> getActiveClientsToday() {
        log.debug("REST request to get active clients today");
        return ResponseEntity.ok(dashboardService.getActiveClientsToday());
    }

    @GetMapping("/transactions/today")
    public ResponseEntity<Long> getTransfersToday() {
        log.debug("REST request to get transfers today");
        return ResponseEntity.ok(dashboardService.getTransfersToday());
    }

    @GetMapping("/transactions/blocked-today")
    public ResponseEntity<Long> getBlockedTransfersToday() {
        log.debug("REST request to get blocked transfers today");
        return ResponseEntity.ok(dashboardService.getBlockedTransfersToday());
    }

    @GetMapping("/statistics/total-clients")
    public ResponseEntity<Long> getTotalClients() {
        log.debug("REST request to get total clients");
        return ResponseEntity.ok(dashboardService.getTotalClients());
    }

    @GetMapping("/statistics/total-transactions")
    public ResponseEntity<Long> getTotalTransactions() {
        log.debug("REST request to get total transactions");
        return ResponseEntity.ok(dashboardService.getTotalTransactions());
    }

    @GetMapping("/statistics/total-beneficiaries")
    public ResponseEntity<Long> getTotalBeneficiaries() {
        log.debug("REST request to get total beneficiaries");
        return ResponseEntity.ok(dashboardService.getTotalBeneficiaries());
    }

    @GetMapping("/reports/monthly")
    public ResponseEntity<MonthlyReportDTO> getMonthlyReport(
            @RequestParam int year,
            @RequestParam int month
    ) {
        log.debug("REST request to get monthly report for {}/{}", month, year);
        return ResponseEntity.ok(dashboardService.getMonthlyReport(year, month));
    }

    @GetMapping("/reports/monthly/transactions.csv")
    public ResponseEntity<byte[]> exportMonthlyTransactionsCsv(
            @RequestParam int year,
            @RequestParam int month
    ) {
        String csv = dashboardService.exportMonthlyTransactionsCsv(year, month);
        String fileName = "transactions-" + year + "-" + String.format("%02d", month) + ".csv";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(csv.getBytes());
    }

    @GetMapping("/reports/monthly/audits.csv")
    public ResponseEntity<byte[]> exportMonthlyAuditsCsv(
            @RequestParam int year,
            @RequestParam int month
    ) {
        String csv = dashboardService.exportMonthlyAuditsCsv(year, month);
        String fileName = "audits-" + year + "-" + String.format("%02d", month) + ".csv";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(csv.getBytes());
    }
}
