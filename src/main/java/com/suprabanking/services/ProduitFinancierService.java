package com.suprabanking.services;

import com.suprabanking.services.dto.ProduitFinancierDTO;

import java.util.List;
import java.util.Optional;

public interface ProduitFinancierService {

    ProduitFinancierDTO saveProduitFinancier(ProduitFinancierDTO produitFinancierDTO);

    ProduitFinancierDTO updateProduitFinancier(ProduitFinancierDTO produitFinancierDTO, Long id);

    ProduitFinancierDTO partialUpdateProduitFinancier(ProduitFinancierDTO produitFinancierDTO, Long id);

    List<ProduitFinancierDTO> findAllProduitFinanciers();

    Optional<ProduitFinancierDTO> findOne(Long id);

    void delete(Long id);
}
