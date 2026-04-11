package com.suprabanking.services;

import com.suprabanking.services.dto.OperationAuditDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface OperationAuditService {
    List<OperationAuditDTO> findMyAudits();
    Page<OperationAuditDTO> findMyAudits(Pageable pageable);
}
