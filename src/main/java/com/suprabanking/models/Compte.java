package com.suprabanking.models;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "comptes")
public class Compte implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String numeroCompte;

    @Column(name = "type")
    private String type; // courant, épargne, etc.

    @Column(name = "solde")
    private Double solde;

    @Column(name = "date_creation")
    private LocalDateTime dateCreation;

    @ManyToOne
    private Client client;

    @OneToMany(mappedBy = "compte")
    private List<Transaction> transactions;
}
