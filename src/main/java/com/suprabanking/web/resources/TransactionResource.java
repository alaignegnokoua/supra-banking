package com.suprabanking.web.resources;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.suprabanking.services.TransactionService;
import com.suprabanking.services.dto.TransactionDTO;
import com.suprabanking.services.dto.TransferLimitStatusDTO;
import com.suprabanking.services.dto.TransferRiskAssessmentDTO;
import com.suprabanking.services.dto.VirementExterneRequest;
import com.suprabanking.services.dto.VirementInterneRequest;
import com.suprabanking.services.dto.OperationAuditDTO;
import com.suprabanking.services.dto.OperationAuditDTO;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/transactions")
public class TransactionResource {

    private final TransactionService transactionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionDTO save(@Valid @RequestBody TransactionDTO dto) {
        log.debug("REST request to save Transaction : {}", dto);
        return transactionService.saveTransaction(dto);
    }

    @PostMapping("/me/virement-interne")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void effectuerVirementInterne(@Valid @RequestBody VirementInterneRequest request) {
        log.debug("REST request to process internal transfer: {}", request);
        transactionService.effectuerVirementInterne(request);
    }

    @PostMapping("/me/virement-externe")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void effectuerVirementExterne(@Valid @RequestBody VirementExterneRequest request) {
        log.debug("REST request to process external transfer: {}", request);
        transactionService.effectuerVirementExterne(request);
    }

    @GetMapping("/me/limits")
    public TransferLimitStatusDTO getMyTransferLimits() {
        log.debug("REST request to get current client transfer limits");
        return transactionService.getMyTransferLimits();
    }

    @GetMapping("/me/risk-preview")
    public TransferRiskAssessmentDTO getMyTransferRiskPreview(
            @RequestParam Double montant,
            @RequestParam(required = false, defaultValue = "EXTERNE") String type
    ) {
        log.debug("REST request to get transfer risk preview for montant={}, type={}", montant, type);
        return transactionService.getMyTransferRiskPreview(montant, type);
    }

    @GetMapping("/me/audit")
    public Page<OperationAuditDTO> getMyTransferAuditHistory(Pageable pageable) {
        log.debug("REST request to get current client transfer audit history");
        return transactionService.getMyTransferAuditHistory(pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionDTO> getById(@PathVariable Long id) {
        log.debug("REST request to get Transaction by id : {}", id);
        return transactionService.findOne(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public Page<TransactionDTO> getAll(Pageable pageable) {
        log.debug("REST request to get all Transactions");
        return transactionService.findAllTransactions(pageable);
    }

    @GetMapping("/me/compte/{compteId}")
    public Page<TransactionDTO> getMyTransactionsByCompte(
            @PathVariable Long compteId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTo,
            @RequestParam(required = false) Double montantMin,
            @RequestParam(required = false) Double montantMax,
            Pageable pageable
    ) {
        log.debug("REST request to get current client transactions by compte {}", compteId);
        return transactionService.findMyTransactionsByCompte(compteId, type, dateFrom, dateTo, montantMin, montantMax, pageable);
    }

    @PutMapping("/{id}")
    public TransactionDTO update(@PathVariable Long id, @Valid @RequestBody TransactionDTO dto) {
        log.debug("REST request to update Transaction : {}", dto);
        return transactionService.updateTransaction(dto, id);
    }

    @PatchMapping("/{id}")
    public TransactionDTO partialUpdate(@PathVariable Long id, @RequestBody TransactionDTO dto) {
        log.debug("REST request to partial update Transaction : {}", dto);
        return transactionService.partialUpdateTransaction(dto, id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        log.debug("REST request to delete Transaction : {}", id);
        transactionService.delete(id);
    }
}
