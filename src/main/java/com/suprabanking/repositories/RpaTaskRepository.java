package com.suprabanking.repositories;

import com.suprabanking.models.RpaTask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RpaTaskRepository extends JpaRepository<RpaTask, Long> {
    List<RpaTask> findByStatut(String statut);
    List<RpaTask> findByType(String type);
    List<RpaTask> findByUser_Id(Long userId);
}