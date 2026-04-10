package com.suprabanking.repositories;

import com.suprabanking.models.Compte;
import com.suprabanking.models.Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CompteRepository extends JpaRepository<Compte, Long> {
    List<Compte> findByClient(Client client);
    Page<Compte> findByClient_Id(Long clientId, Pageable pageable);
    Optional<Compte> findByNumeroCompte(String numeroCompte);
    boolean existsByNumeroCompte(String numeroCompte);
}
