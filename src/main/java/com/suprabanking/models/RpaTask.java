package com.suprabanking.models;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "rpa_tasks")
public class RpaTask implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nom_tache")
    private String nomTache;

    @Column(name = "statut")
    private String statut; // en_attente, terminé, échoué

    @Column(name = "date_execution")
    private LocalDateTime dateExecution;
}
