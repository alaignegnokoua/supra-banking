package com.suprabanking.services.mapper;

import com.suprabanking.models.Compte;
import com.suprabanking.services.dto.CompteDTO;

public interface CompteMapper {

    Compte toEntity(CompteDTO dto);

    CompteDTO toDto(Compte entity);

    void partialUpdate(Compte entity, CompteDTO dto);
}
