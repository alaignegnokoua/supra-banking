package com.suprabanking.services.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BeneficiaryUsageHistoryDTO implements Serializable {

    private Long id;

    private Long beneficiaireId;

    private String beneficiaireName;

    private Long transactionId;

    private Double montant;

    private String typeOperation;

    private String status;

    private LocalDateTime usedAt;

    private LocalDateTime createdAt;
}
