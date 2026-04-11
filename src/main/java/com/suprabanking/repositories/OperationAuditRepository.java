package com.suprabanking.repositories;

import com.suprabanking.models.OperationAudit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OperationAuditRepository extends JpaRepository<OperationAudit, Long> {
}
