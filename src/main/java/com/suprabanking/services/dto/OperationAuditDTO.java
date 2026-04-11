package com.suprabanking.services.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OperationAuditDTO implements Serializable {

    private Long id;
    private String operationType;
    private String status;
    private String message;
    private LocalDateTime createdAt;
    private Long clientId;
    private Long compteSourceId;
    private Long compteDestinationId;
    private Long beneficiaireId;
    private Double montant;
    private Integer riskScore;
    private String riskLevel;
    private Boolean riskBlocked;
    private Map<String, Object> riskDetails;
}
