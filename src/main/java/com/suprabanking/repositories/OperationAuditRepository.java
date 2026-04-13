package com.suprabanking.repositories;

import com.suprabanking.models.OperationAudit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OperationAuditRepository extends JpaRepository<OperationAudit, Long> {
	List<OperationAudit> findByClientIdOrderByCreatedAtDesc(Long clientId);
	Page<OperationAudit> findByClientIdOrderByCreatedAtDesc(Long clientId, Pageable pageable);
	List<OperationAudit> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime startDate, LocalDateTime endDate);

	@Query("""
		select count(a)
		from OperationAudit a
		where a.status = :status
		  and a.createdAt >= :startDate
		  and a.createdAt <= :endDate
		""")
	long countByStatusAndDateRange(
		@Param("status") String status,
		@Param("startDate") LocalDateTime startDate,
		@Param("endDate") LocalDateTime endDate
	);
}
