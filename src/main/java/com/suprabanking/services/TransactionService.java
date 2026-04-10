package com.suprabanking.services;

import com.suprabanking.services.dto.TransactionDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface TransactionService {

    TransactionDTO saveTransaction(TransactionDTO transactionDTO);

    TransactionDTO updateTransaction(TransactionDTO transactionDTO, Long id);

    TransactionDTO partialUpdateTransaction(TransactionDTO transactionDTO, Long id);

    Page<TransactionDTO> findAllTransactions(Pageable pageable);

    Optional<TransactionDTO> findOne(Long id);

    void delete(Long id);
}
