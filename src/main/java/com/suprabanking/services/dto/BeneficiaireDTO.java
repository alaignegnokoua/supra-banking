package com.suprabanking.services.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BeneficiaireDTO implements Serializable {

    private Long id;

    @NotBlank(message = "Le nom du bénéficiaire est obligatoire")
    private String nom;

    private String iban;

    private String rib;

    private String banque;

    @Email(message = "Format d'email invalide")
    private String email;

    private LocalDateTime createdAt;

    // Usage statistics
    private Long successfulTransfersCount;

    private LocalDateTime lastUsedAt;

    private String status; // ACTIVE, PENDING_VERIFICATION, BLOCKED, etc.
}
