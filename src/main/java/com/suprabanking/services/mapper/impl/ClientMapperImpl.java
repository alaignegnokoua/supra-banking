package com.suprabanking.services.mapper.impl;

import com.suprabanking.models.Client;
import com.suprabanking.services.dto.ClientDTO;
import com.suprabanking.services.mapper.ClientMapper;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ClientMapperImpl implements ClientMapper {

    private final ModelMapper modelMapper;

    @Override
    public Client toEntity(ClientDTO dto) {
        return modelMapper.map(dto, Client.class);
    }

    @Override
    public ClientDTO toDto(Client entity) {
        return modelMapper.map(entity, ClientDTO.class);
    }

    @Override
    public void partialUpdate(Client entity, ClientDTO dto) {
        if (dto.getNom() != null) {
            entity.setNom(dto.getNom());
        }
        if (dto.getPrenom() != null) {
            entity.setPrenom(dto.getPrenom());
        }
        if (dto.getEmail() != null) {
            entity.setEmail(dto.getEmail());
        }
        if (dto.getTelephone() != null) {
            entity.setTelephone(dto.getTelephone());
        }
        if (dto.getIdentifiant() != null) {
            entity.setIdentifiant(dto.getIdentifiant());
        }
        if (dto.getRiskProfile() != null) {
            entity.setRiskProfile(dto.getRiskProfile());
        }
        if (dto.getCustomMaxSingleTransferAmount() != null) {
            entity.setCustomMaxSingleTransferAmount(dto.getCustomMaxSingleTransferAmount());
        }
        if (dto.getCustomMaxDailyTransferTotal() != null) {
            entity.setCustomMaxDailyTransferTotal(dto.getCustomMaxDailyTransferTotal());
        }
        if (dto.getCustomMaxDailyTransferCount() != null) {
            entity.setCustomMaxDailyTransferCount(dto.getCustomMaxDailyTransferCount());
        }
        if (dto.getCustomMinTransferIntervalSeconds() != null) {
            entity.setCustomMinTransferIntervalSeconds(dto.getCustomMinTransferIntervalSeconds());
        }
    }
}
