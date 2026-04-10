package com.suprabanking.services.mapper;

import com.suprabanking.models.Transaction;
import com.suprabanking.services.dto.TransactionDTO;

public interface TransactionMapper {

    Transaction toEntity(TransactionDTO dto);

    TransactionDTO toDto(Transaction entity);

    void partialUpdate(Transaction entity, TransactionDTO dto);
}
