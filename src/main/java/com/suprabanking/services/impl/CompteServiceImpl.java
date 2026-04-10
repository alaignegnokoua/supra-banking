package com.suprabanking.services.impl;

import com.suprabanking.config.security.CurrentUserService;
import com.suprabanking.models.Client;
import com.suprabanking.models.Compte;
import com.suprabanking.repositories.ClientRepository;
import com.suprabanking.repositories.CompteRepository;
import com.suprabanking.services.CompteService;
import com.suprabanking.services.dto.CompteDTO;
import com.suprabanking.services.mapper.CompteMapper;
import com.suprabanking.web.errors.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompteServiceImpl implements CompteService {

    private final CompteRepository compteRepository;
    private final ClientRepository clientRepository;
    private final CompteMapper compteMapper;
    private final CurrentUserService currentUserService;

    @Override
    public CompteDTO saveCompte(CompteDTO dto) {
        log.debug("Request to save Compte : {}", dto);
        if (compteRepository.existsByNumeroCompte(dto.getNumeroCompte())) {
            throw new IllegalArgumentException("Numéro de compte déjà utilisé");
        }

        Client client = clientRepository.findById(dto.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id=" + dto.getClientId()));

        Compte entity = compteMapper.toEntity(dto);
        entity.setClient(client);
        if (entity.getDateCreation() == null) {
            entity.setDateCreation(LocalDateTime.now());
        }

        return compteMapper.toDto(compteRepository.save(entity));
    }

    @Override
    public CompteDTO updateCompte(CompteDTO dto, Long id) {
        log.debug("Request to update Compte : {}", dto);
        Compte entity = compteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Compte not found with id=" + id));

        Client client = clientRepository.findById(dto.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id=" + dto.getClientId()));

        entity.setNumeroCompte(dto.getNumeroCompte());
        entity.setType(dto.getType());
        entity.setSolde(dto.getSolde());
        entity.setDateCreation(dto.getDateCreation() != null ? dto.getDateCreation() : entity.getDateCreation());
        entity.setClient(client);

        return compteMapper.toDto(compteRepository.save(entity));
    }

    @Override
    public CompteDTO partialUpdateCompte(CompteDTO dto, Long id) {
        log.debug("Request to partial update Compte : {}", dto);
        return compteRepository.findById(id)
                .map(existing -> {
                    compteMapper.partialUpdate(existing, dto);
                    if (dto.getClientId() != null) {
                        Client client = clientRepository.findById(dto.getClientId())
                                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id=" + dto.getClientId()));
                        existing.setClient(client);
                    }
                    return compteMapper.toDto(compteRepository.save(existing));
                })
                .orElseThrow(() -> new ResourceNotFoundException("Compte not found with id=" + id));
    }

    @Override
    public Page<CompteDTO> findAllComptes(Pageable pageable) {
        log.debug("Request to get all Comptes");

        if (currentUserService.isCurrentUserClient()) {
            return findMyComptes(pageable);
        }

        return compteRepository.findAll(pageable).map(compteMapper::toDto);
    }

    @Override
    public Page<CompteDTO> findMyComptes(Pageable pageable) {
        log.debug("Request to get current client Comptes");
        Long clientId = currentUserService.requireCurrentClientId();
        return compteRepository.findByClient_Id(clientId, pageable).map(compteMapper::toDto);
    }

    @Override
    public Optional<CompteDTO> findOne(Long id) {
        log.debug("Request to get Compte : {}", id);
        Compte compte = compteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Compte not found with id=" + id));

        if (currentUserService.isCurrentUserClient()) {
            assertOwnershipForCurrentClient(compte);
        }

        return Optional.of(compteMapper.toDto(compte));
    }

    @Override
    public Optional<CompteDTO> findMyOne(Long id) {
        log.debug("Request to get current client Compte : {}", id);
        Compte compte = compteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Compte not found with id=" + id));
        assertOwnershipForCurrentClient(compte);
        return Optional.of(compteMapper.toDto(compte));
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete Compte : {}", id);
        compteRepository.deleteById(id);
    }

    private void assertOwnershipForCurrentClient(Compte compte) {
        Long clientId = currentUserService.requireCurrentClientId();
        if (compte.getClient() == null || !clientId.equals(compte.getClient().getId())) {
            throw new AccessDeniedException("Accès refusé à ce compte");
        }
    }
}
