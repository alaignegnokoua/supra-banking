package com.suprabanking.services.impl;

import com.suprabanking.models.Client;
import com.suprabanking.models.ProduitFinancier;
import com.suprabanking.repositories.ClientRepository;
import com.suprabanking.repositories.ProduitFinancierRepository;
import com.suprabanking.services.ProduitFinancierService;
import com.suprabanking.services.dto.ProduitFinancierDTO;
import com.suprabanking.services.mapper.ProduitFinancierMapper;
import com.suprabanking.web.errors.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
@Service
public class ProduitFinancierServiceImpl implements ProduitFinancierService {

    private final ProduitFinancierRepository produitFinancierRepository;
    private final ClientRepository clientRepository;
    private final ProduitFinancierMapper produitFinancierMapper;

    @Override
    public ProduitFinancierDTO saveProduitFinancier(ProduitFinancierDTO dto) {
        log.debug("Request to save ProduitFinancier : {}", dto);
        ProduitFinancier entity = produitFinancierMapper.toEntity(dto);

        if(dto.getClientId() != null){
            Client client = clientRepository.findById(dto.getClientId())
                    .orElseThrow(() -> new ResourceNotFoundException("Client not found with id=" + dto.getClientId()));
            entity.setClient(client);
        }

        entity = produitFinancierRepository.save(entity);
        return produitFinancierMapper.toDto(entity);
    }

    @Override
    public ProduitFinancierDTO updateProduitFinancier(ProduitFinancierDTO dto, Long id) {
        log.debug("Request to update ProduitFinancier : {}", dto);
        ProduitFinancier entity = produitFinancierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProduitFinancier not found with id=" + id));
        entity.setCodeProduit(dto.getCodeProduit());
        entity.setType(dto.getType());
        entity.setMontant(dto.getMontant());
        entity.setStatut(dto.getStatut());
        if (dto.getClientId() != null) {
            Client client = clientRepository.findById(dto.getClientId())
                    .orElseThrow(() -> new ResourceNotFoundException("Client not found with id=" + dto.getClientId()));
            entity.setClient(client);
        } else {
            entity.setClient(null);
        }
        entity = produitFinancierRepository.save(entity);
        return produitFinancierMapper.toDto(entity);
    }

    @Override
    public ProduitFinancierDTO partialUpdateProduitFinancier(ProduitFinancierDTO dto, Long id) {
        log.debug("Request to partial update ProduitFinancier : {}", dto);
        return produitFinancierRepository.findById(id)
                .map(existing -> {
                    produitFinancierMapper.partialUpdate(existing, dto);
                    if (dto.getClientId() != null) {
                        Client client = clientRepository.findById(dto.getClientId())
                                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id=" + dto.getClientId()));
                        existing.setClient(client);
                    }
                    return produitFinancierMapper.toDto(produitFinancierRepository.save(existing));
                })
                .orElseThrow(() -> new ResourceNotFoundException("ProduitFinancier not found with id=" + id));
    }

    @Override
    public List<ProduitFinancierDTO> findAllProduitFinanciers() {
        log.debug("Request to get all ProduitFinanciers");
        return produitFinancierRepository.findAll()
                .stream()
                .map(produitFinancierMapper::toDto)
                .toList();
    }

    @Override
    public Optional<ProduitFinancierDTO> findOne(Long id) {
        log.debug("Request to get ProduitFinancier : {}", id);
        return produitFinancierRepository.findById(id)
                .map(produitFinancierMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete ProduitFinancier : {}", id);
        produitFinancierRepository.deleteById(id);
    }
}
