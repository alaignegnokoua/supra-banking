package com.suprabanking.services.impl;

import com.suprabanking.config.security.CurrentUserService;
import com.suprabanking.models.Client;
import com.suprabanking.models.Compte;
import com.suprabanking.models.Transaction;
import com.suprabanking.repositories.ClientRepository;
import com.suprabanking.repositories.CompteRepository;
import com.suprabanking.repositories.TransactionRepository;
import com.suprabanking.services.TransactionService;
import com.suprabanking.services.dto.TransactionDTO;
import com.suprabanking.services.mapper.TransactionMapper;
import com.suprabanking.web.errors.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final CompteRepository compteRepository;
    private final ClientRepository clientRepository;
    private final TransactionMapper transactionMapper;
    private final CurrentUserService currentUserService;

    @Override
    public TransactionDTO saveTransaction(TransactionDTO dto) {
        log.debug("Request to save Transaction : {}", dto);

        Compte compte = compteRepository.findById(dto.getCompteId())
                .orElseThrow(() -> new ResourceNotFoundException("Compte not found with id=" + dto.getCompteId()));

        Client client;
        if (dto.getClientId() != null) {
            client = clientRepository.findById(dto.getClientId())
                    .orElseThrow(() -> new ResourceNotFoundException("Client not found with id=" + dto.getClientId()));
        } else {
            client = compte.getClient();
        }

        Transaction entity = transactionMapper.toEntity(dto);
        entity.setCompte(compte);
        entity.setClient(client);
        if (entity.getDateTransaction() == null) {
            entity.setDateTransaction(LocalDateTime.now());
        }

        return transactionMapper.toDto(transactionRepository.save(entity));
    }

    @Override
    public TransactionDTO updateTransaction(TransactionDTO dto, Long id) {
        log.debug("Request to update Transaction : {}", dto);
        Transaction entity = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id=" + id));

        Compte compte = compteRepository.findById(dto.getCompteId())
                .orElseThrow(() -> new ResourceNotFoundException("Compte not found with id=" + dto.getCompteId()));

        Client client;
        if (dto.getClientId() != null) {
            client = clientRepository.findById(dto.getClientId())
                    .orElseThrow(() -> new ResourceNotFoundException("Client not found with id=" + dto.getClientId()));
        } else {
            client = compte.getClient();
        }

        entity.setType(dto.getType());
        entity.setMontant(dto.getMontant());
        entity.setDateTransaction(dto.getDateTransaction() != null ? dto.getDateTransaction() : entity.getDateTransaction());
        entity.setDescription(dto.getDescription());
        entity.setCompte(compte);
        entity.setClient(client);

        return transactionMapper.toDto(transactionRepository.save(entity));
    }

    @Override
    public TransactionDTO partialUpdateTransaction(TransactionDTO dto, Long id) {
        log.debug("Request to partial update Transaction : {}", dto);
        return transactionRepository.findById(id)
                .map(existing -> {
                    transactionMapper.partialUpdate(existing, dto);

                    if (dto.getCompteId() != null) {
                        Compte compte = compteRepository.findById(dto.getCompteId())
                                .orElseThrow(() -> new ResourceNotFoundException("Compte not found with id=" + dto.getCompteId()));
                        existing.setCompte(compte);
                        if (dto.getClientId() == null && existing.getClient() == null) {
                            existing.setClient(compte.getClient());
                        }
                    }

                    if (dto.getClientId() != null) {
                        Client client = clientRepository.findById(dto.getClientId())
                                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id=" + dto.getClientId()));
                        existing.setClient(client);
                    }

                    return transactionMapper.toDto(transactionRepository.save(existing));
                })
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id=" + id));
    }

    @Override
    public Page<TransactionDTO> findAllTransactions(Pageable pageable) {
        log.debug("Request to get all Transactions");

        if (currentUserService.isCurrentUserClient()) {
            Long clientId = currentUserService.requireCurrentClientId();
            return transactionRepository.findByClient_Id(clientId, pageable).map(transactionMapper::toDto);
        }

        return transactionRepository.findAll(pageable).map(transactionMapper::toDto);
    }

    @Override
    public Optional<TransactionDTO> findOne(Long id) {
        log.debug("Request to get Transaction : {}", id);
        if (currentUserService.isCurrentUserClient()) {
            Long clientId = currentUserService.requireCurrentClientId();
            return transactionRepository.findByIdAndClient_Id(id, clientId)
                    .map(transactionMapper::toDto);
        }

        return transactionRepository.findById(id).map(transactionMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete Transaction : {}", id);
        transactionRepository.deleteById(id);
    }
}
