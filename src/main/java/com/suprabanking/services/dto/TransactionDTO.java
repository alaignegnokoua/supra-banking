package com.suprabanking.services.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
public class TransactionDTO implements Serializable {

    private Long id;

    @NotBlank(message = "Le type est obligatoire")
    private String type;

    @NotNull(message = "Le montant est obligatoire")
    @Positive(message = "Le montant doit être strictement positif")
    private Double montant;

    private LocalDateTime dateTransaction;

    private String description;

    private Long clientId;

    @NotNull(message = "Le compteId est obligatoire")
    private Long compteId;
}
