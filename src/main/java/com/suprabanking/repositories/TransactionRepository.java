package com.suprabanking.repositories;

import com.suprabanking.models.Transaction;
import com.suprabanking.models.Compte;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByCompte(Compte compte);
    List<Transaction> findByType(String type);
    List<Transaction> findByCompteId(Long compteId);
    List<Transaction> findByClient_Id(Long clientId);
}
