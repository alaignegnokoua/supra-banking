package com.suprabanking.repositories;

import com.suprabanking.models.Client;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {
    Optional<Client> findByTelephone(String telephone);
    Optional<Client> findByEmail(String email);
    boolean existsByTelephone(String telephone);
    boolean existsByEmail(String email);
}