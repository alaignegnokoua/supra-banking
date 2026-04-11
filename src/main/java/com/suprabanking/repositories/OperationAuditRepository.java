package com.suprabanking.repositories;

import com.suprabanking.models.OperationAudit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OperationAuditRepository extends JpaRepository<OperationAudit, Long> {
	List<OperationAudit> findByClientIdOrderByCreatedAtDesc(Long clientId);
}
