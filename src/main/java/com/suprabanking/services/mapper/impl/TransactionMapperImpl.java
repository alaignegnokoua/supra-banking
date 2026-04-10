package com.suprabanking.services.mapper.impl;

import com.suprabanking.models.Transaction;
import com.suprabanking.services.dto.TransactionDTO;
import com.suprabanking.services.mapper.TransactionMapper;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TransactionMapperImpl implements TransactionMapper {

    private final ModelMapper modelMapper;

    @Override
    public Transaction toEntity(TransactionDTO dto) {
        return modelMapper.map(dto, Transaction.class);
    }

    @Override
    public TransactionDTO toDto(Transaction entity) {
        TransactionDTO dto = modelMapper.map(entity, TransactionDTO.class);
        if (entity.getClient() != null) {
            dto.setClientId(entity.getClient().getId());
        }
        if (entity.getCompte() != null) {
            dto.setCompteId(entity.getCompte().getId());
        }
        return dto;
    }

    @Override
    public void partialUpdate(Transaction entity, TransactionDTO dto) {
        if (dto.getType() != null) {
            entity.setType(dto.getType());
        }
        if (dto.getMontant() != null) {
            entity.setMontant(dto.getMontant());
        }
        if (dto.getDateTransaction() != null) {
            entity.setDateTransaction(dto.getDateTransaction());
        }
        if (dto.getDescription() != null) {
            entity.setDescription(dto.getDescription());
        }
    }
}
