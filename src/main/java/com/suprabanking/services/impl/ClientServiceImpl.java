package com.suprabanking.services.impl;

import com.suprabanking.models.Client;
import com.suprabanking.repositories.ClientRepository;
import com.suprabanking.services.ClientService;
import com.suprabanking.services.dto.ClientDTO;
import com.suprabanking.services.mapper.ClientMapper;
import com.suprabanking.web.errors.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;
    private final ClientMapper clientMapper;

    @Override
    public ClientDTO saveClient(ClientDTO dto) {
        log.debug("Request to save Client : {}", dto);
        if (clientRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email déjà utilisé");
        }
        if (clientRepository.existsByTelephone(dto.getTelephone())) {
            throw new IllegalArgumentException("Téléphone déjà utilisé");
        }
        Client entity = clientMapper.toEntity(dto);
        entity.setMotDePasse(null);
        return clientMapper.toDto(clientRepository.save(entity));
    }

    @Override
    public ClientDTO updateClient(ClientDTO dto, Long id) {
        log.debug("Request to update Client : {}", dto);
        Client entity = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id=" + id));

        entity.setNom(dto.getNom());
        entity.setPrenom(dto.getPrenom());
        entity.setEmail(dto.getEmail());
        entity.setTelephone(dto.getTelephone());
        entity.setIdentifiant(dto.getIdentifiant());

        return clientMapper.toDto(clientRepository.save(entity));
    }

    @Override
    public ClientDTO partialUpdateClient(ClientDTO dto, Long id) {
        log.debug("Request to partial update Client : {}", dto);
        return clientRepository.findById(id)
                .map(existing -> {
                    clientMapper.partialUpdate(existing, dto);
                    return clientMapper.toDto(clientRepository.save(existing));
                })
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id=" + id));
    }

    @Override
    public Page<ClientDTO> findAllClients(Pageable pageable) {
        log.debug("Request to get all Clients");
        return clientRepository.findAll(pageable).map(clientMapper::toDto);
    }

    @Override
    public Optional<ClientDTO> findOne(Long id) {
        log.debug("Request to get Client : {}", id);
        return clientRepository.findById(id).map(clientMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete Client : {}", id);
        clientRepository.deleteById(id);
    }
}
