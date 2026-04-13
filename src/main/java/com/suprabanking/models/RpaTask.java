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

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    @Column(name = "type")
    private String type;

    @Column(name = "payload", length = 2000)
    private String payload;

    @Column(name = "result_message", length = 1000)
    private String resultMessage;

    @Column(name = "last_error", length = 1000)
    private String lastError;

    @Column(name = "retry_count")
    private Integer retryCount;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
