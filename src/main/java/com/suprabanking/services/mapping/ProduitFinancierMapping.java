package com.suprabanking.services.mapping;

import com.suprabanking.models.ProduitFinancier;
import com.suprabanking.services.dto.ProduitFinancierDTO;

public final class ProduitFinancierMapping {

    private ProduitFinancierMapping() {}

    public static void partialUpdate(ProduitFinancier entity, ProduitFinancierDTO dto) {
        if(dto.getCodeProduit() != null){
            entity.setCodeProduit(dto.getCodeProduit());
        }
        if(dto.getType() != null){
            entity.setType(dto.getType());
        }
        if(dto.getMontant() != null){
            entity.setMontant(dto.getMontant());
        }
        if(dto.getStatut() != null){
            entity.setStatut(dto.getStatut());
        }
    }
}
