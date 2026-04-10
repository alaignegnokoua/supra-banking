package com.suprabanking.repositories;

import com.suprabanking.models.ProduitFinancier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProduitFinancierRepository extends JpaRepository<ProduitFinancier, Long> {
    Optional<ProduitFinancier> findByCodeProduit(String codeProduit);
    List<ProduitFinancier> findByClient_Id(Long clientId);
    boolean existsByCodeProduit(String codeProduit);
}