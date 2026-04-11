package com.suprabanking.services;

import com.suprabanking.services.dto.OperationAuditDTO;

import java.util.List;

public interface OperationAuditService {
    List<OperationAuditDTO> findMyAudits();
}
