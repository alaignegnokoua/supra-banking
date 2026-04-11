package com.suprabanking.models;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;


@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "clients")
public class Client implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nom")
    private String nom;

    @Column(name = "prenom")
    private String prenom;

    @Column(name = "email")
    private String email;

    @Column(name = "telephone")
    private String telephone;

    @Column(name = "identifiant", unique = true)
    private String identifiant;

    @Column(name = "mot_de_passe")
    private String motDePasse;

    @Column(name = "notifications_in_app_enabled")
    private Boolean notificationsInAppEnabled;

    @Column(name = "notifications_email_enabled")
    private Boolean notificationsEmailEnabled;

    @Column(name = "risk_profile")
    private String riskProfile;

    @OneToMany(mappedBy = "client")
    private List<Compte> comptes;

    @OneToMany(mappedBy = "client")
    private List<Notification> notifications;

    @OneToMany(mappedBy = "client")
    private List<ProduitFinancier> produits;

    @OneToMany(mappedBy = "client")
    private List<BotQuery> botQueries;
}