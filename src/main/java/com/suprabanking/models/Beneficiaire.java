package com.suprabanking.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "beneficiaires")
public class Beneficiaire implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nom", nullable = false)
    private String nom;

    @Column(name = "iban")
    private String iban;

    @Column(name = "rib")
    private String rib;

    @Column(name = "banque")
    private String banque;

    @Column(name = "email")
    private String email;

    @Column(name = "status", nullable = false)
    private String status = "ACTIVE"; // ACTIVE, PENDING_VERIFICATION, BLOCKED

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @OneToMany(mappedBy = "beneficiaire", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<BeneficiaryUsageHistory> usageHistory;
}
