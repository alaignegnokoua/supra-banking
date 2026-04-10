package com.suprabanking.services.mapper.impl;

import com.suprabanking.models.Compte;
import com.suprabanking.services.dto.CompteDTO;
import com.suprabanking.services.mapper.CompteMapper;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CompteMapperImpl implements CompteMapper {

    private final ModelMapper modelMapper;

    @Override
    public Compte toEntity(CompteDTO dto) {
        return modelMapper.map(dto, Compte.class);
    }

    @Override
    public CompteDTO toDto(Compte entity) {
        CompteDTO dto = modelMapper.map(entity, CompteDTO.class);
        if (entity.getClient() != null) {
            dto.setClientId(entity.getClient().getId());
        }
        return dto;
    }

    @Override
    public void partialUpdate(Compte entity, CompteDTO dto) {
        if (dto.getNumeroCompte() != null) {
            entity.setNumeroCompte(dto.getNumeroCompte());
        }
        if (dto.getType() != null) {
            entity.setType(dto.getType());
        }
        if (dto.getSolde() != null) {
            entity.setSolde(dto.getSolde());
        }
        if (dto.getDateCreation() != null) {
            entity.setDateCreation(dto.getDateCreation());
        }
    }
}
