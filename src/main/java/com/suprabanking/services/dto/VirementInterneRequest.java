package com.suprabanking.services.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VirementInterneRequest {

    @NotNull(message = "Le compte source est obligatoire")
    private Long compteSourceId;

    @NotNull(message = "Le compte destination est obligatoire")
    private Long compteDestinationId;

    @NotNull(message = "Le montant est obligatoire")
    @Positive(message = "Le montant doit être strictement positif")
    private Double montant;

    private String description;
}
