package com.suprabanking.services.mapper;

import com.suprabanking.models.ProduitFinancier;
import com.suprabanking.services.dto.ProduitFinancierDTO;

public interface ProduitFinancierMapper {

    ProduitFinancier toEntity(ProduitFinancierDTO dto);

    ProduitFinancierDTO toDto(ProduitFinancier entity);

    void partialUpdate(ProduitFinancier entity, ProduitFinancierDTO dto);
}
