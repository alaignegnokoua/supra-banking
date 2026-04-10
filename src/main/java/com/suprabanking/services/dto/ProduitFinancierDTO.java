package com.suprabanking.services.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProduitFinancierDTO implements Serializable {

    private Long id;
    @NotBlank(message = "Le code produit est obligatoire")
    private String codeProduit;
    @NotBlank(message = "Le type est obligatoire")
    private String type; // prêt, dépôt à terme...
    @NotNull(message = "Le montant est obligatoire")
    @PositiveOrZero(message = "Le montant doit être positif")
    private Double montant;
    @NotBlank(message = "Le statut est obligatoire")
    private String statut; // actif, soldé...
    private Long clientId; // référence au client propriétaire
}
