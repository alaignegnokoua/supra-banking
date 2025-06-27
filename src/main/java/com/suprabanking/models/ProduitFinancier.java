package com.suprabanking.models;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "produits_financiers")
public class ProduitFinancier implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code_produit", unique = true)
    private String codeProduit;

    @Column(name = "type")
    private String type; // prêt, dépôt à terme...

    @Column(name = "montant")
    private Double montant;

    @Column(name = "statut")
    private String statut; // actif, soldé...

    @ManyToOne
    private Client client;
}
