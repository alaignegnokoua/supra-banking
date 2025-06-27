package com.suprabanking.services.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProduitFinancierDTO implements Serializable {

    private Long id;
    private String codeProduit;
    private String type; // prêt, dépôt à terme...
    private Double montant;
    private String statut; // actif, soldé...
    private Long clientId; // référence au client propriétaire
}
