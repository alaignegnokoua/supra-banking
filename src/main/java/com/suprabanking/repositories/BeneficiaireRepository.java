package com.suprabanking.repositories;

import com.suprabanking.models.Beneficiaire;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BeneficiaireRepository extends JpaRepository<Beneficiaire, Long> {
    List<Beneficiaire> findByClient_Id(Long clientId);
    Optional<Beneficiaire> findByIdAndClient_Id(Long id, Long clientId);
    boolean existsByClient_IdAndIban(Long clientId, String iban);
    boolean existsByClient_IdAndRib(Long clientId, String rib);
}
