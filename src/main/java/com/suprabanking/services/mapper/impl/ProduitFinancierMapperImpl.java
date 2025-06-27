package com.suprabanking.services.mapper.impl;

import com.suprabanking.models.ProduitFinancier;
import com.suprabanking.services.dto.ProduitFinancierDTO;
import com.suprabanking.services.mapper.ProduitFinancierMapper;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProduitFinancierMapperImpl implements ProduitFinancierMapper {

    private final ModelMapper modelMapper;

    @Override
    public ProduitFinancier toEntity(ProduitFinancierDTO dto) {
        return modelMapper.map(dto, ProduitFinancier.class);
    }

    @Override
    public ProduitFinancierDTO toDto(ProduitFinancier entity) {
        ProduitFinancierDTO dto = modelMapper.map(entity, ProduitFinancierDTO.class);
        if(entity.getClient() != null){
            dto.setClientId(entity.getClient().getId());
        }
        return dto;
    }

    @Override
    public void partialUpdate(ProduitFinancier entity, ProduitFinancierDTO dto) {
        modelMapper.map(dto, entity);
    }
}
