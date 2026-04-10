package com.suprabanking.web.resources;

import com.suprabanking.services.TransactionService;
import com.suprabanking.services.dto.TransactionDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
