package com.suprabanking.repositories;

import com.suprabanking.models.BotQuery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BotQueryRepository extends JpaRepository<BotQuery, Long> {
    List<BotQuery> findByUser_Id(Long id);
    List<BotQuery> findByEtat(String etat);
}