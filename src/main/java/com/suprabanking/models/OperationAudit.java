package com.suprabanking.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "operation_audits")
public class OperationAudit implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "operation_type", nullable = false)
    private String operationType;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "message", length = 500)
    private String message;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "client_id")
    private Long clientId;

    @Column(name = "compte_source_id")
    private Long compteSourceId;

    @Column(name = "compte_destination_id")
    private Long compteDestinationId;

    @Column(name = "beneficiaire_id")
    private Long beneficiaireId;

    @Column(name = "montant")
    private Double montant;

    @Column(name = "risk_score")
    private Integer riskScore;

    @Column(name = "risk_level")
    private String riskLevel;

    @Column(name = "risk_blocked")
    private Boolean riskBlocked;

    @Column(name = "risk_details", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> riskDetails;
}
