package com.suprabanking.services.impl;

import com.suprabanking.config.security.CurrentUserService;
import com.suprabanking.models.OperationAudit;
import com.suprabanking.repositories.OperationAuditRepository;
import com.suprabanking.services.OperationAuditService;
import com.suprabanking.services.dto.OperationAuditDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OperationAuditServiceImpl implements OperationAuditService {

    private final OperationAuditRepository operationAuditRepository;
    private final CurrentUserService currentUserService;

    @Override
    public List<OperationAuditDTO> findMyAudits() {
        Long clientId = currentUserService.requireCurrentClientId();
        return operationAuditRepository.findByClientIdOrderByCreatedAtDesc(clientId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    private OperationAuditDTO toDto(OperationAudit audit) {
        return new OperationAuditDTO(
                audit.getId(),
                audit.getOperationType(),
                audit.getStatus(),
                audit.getMessage(),
                audit.getCreatedAt(),
                audit.getClientId(),
                audit.getCompteSourceId(),
                audit.getCompteDestinationId(),
                audit.getBeneficiaireId(),
                audit.getMontant()
        );
    }
}
