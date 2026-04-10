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
import com.suprabanking.services.dto.VirementInterneRequest;
import com.suprabanking.services.mapper.TransactionMapper;
import com.suprabanking.web.errors.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional
    public void effectuerVirementInterne(VirementInterneRequest request) {
        log.debug("Request to process internal transfer: {}", request);

        if (request.getCompteSourceId().equals(request.getCompteDestinationId())) {
            throw new IllegalArgumentException("Le compte source et le compte destination doivent être différents");
        }

        Long clientId = currentUserService.requireCurrentClientId();

        Compte compteSource = compteRepository.findById(request.getCompteSourceId())
                .orElseThrow(() -> new ResourceNotFoundException("Compte source introuvable"));

        Compte compteDestination = compteRepository.findById(request.getCompteDestinationId())
                .orElseThrow(() -> new ResourceNotFoundException("Compte destination introuvable"));

        if (compteSource.getClient() == null || !clientId.equals(compteSource.getClient().getId())
                || compteDestination.getClient() == null || !clientId.equals(compteDestination.getClient().getId())) {
            throw new AccessDeniedException("Accès refusé à l'un des comptes");
        }

        if (compteSource.getSolde() == null || compteSource.getSolde() < request.getMontant()) {
            throw new IllegalArgumentException("Solde insuffisant pour effectuer ce virement");
        }

        compteSource.setSolde(compteSource.getSolde() - request.getMontant());
        compteDestination.setSolde(compteDestination.getSolde() + request.getMontant());

        compteRepository.save(compteSource);
        compteRepository.save(compteDestination);

        String descriptionBase = (request.getDescription() == null || request.getDescription().isBlank())
                ? "Virement interne"
                : request.getDescription().trim();

        LocalDateTime now = LocalDateTime.now();
        Client client = compteSource.getClient();

        Transaction debit = new Transaction();
        debit.setType("virement");
        debit.setMontant(request.getMontant());
        debit.setDateTransaction(now);
        debit.setDescription(descriptionBase + " - débit vers " + compteDestination.getNumeroCompte());
        debit.setClient(client);
        debit.setCompte(compteSource);

        Transaction credit = new Transaction();
        credit.setType("virement");
        credit.setMontant(request.getMontant());
        credit.setDateTransaction(now);
        credit.setDescription(descriptionBase + " - crédit depuis " + compteSource.getNumeroCompte());
        credit.setClient(client);
        credit.setCompte(compteDestination);

        transactionRepository.save(debit);
        transactionRepository.save(credit);
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
    public Page<TransactionDTO> findMyTransactionsByCompte(
            Long compteId,
            String type,
            LocalDateTime dateFrom,
            LocalDateTime dateTo,
            Double montantMin,
            Double montantMax,
            Pageable pageable
    ) {
        log.debug("Request to get current client transactions by compte {}, filters type={}, from={}, to={}, min={}, max={}",
            compteId, type, dateFrom, dateTo, montantMin, montantMax);

        Long clientId = currentUserService.requireCurrentClientId();

        Compte compte = compteRepository.findById(compteId)
            .orElseThrow(() -> new ResourceNotFoundException("Compte not found with id=" + compteId));

        if (compte.getClient() == null || !clientId.equals(compte.getClient().getId())) {
            throw new AccessDeniedException("Accès refusé à ce compte");
        }

        return transactionRepository.findMyTransactionsByCompteWithFilters(
                clientId,
                compteId,
                type,
                dateFrom,
                dateTo,
                montantMin,
                montantMax,
                pageable
        ).map(transactionMapper::toDto);
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
