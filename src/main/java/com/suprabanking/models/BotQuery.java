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
@Table(name = "bot_queries")
public class BotQuery implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "question")
    private String question;

    @Column(name = "etat")
    private String etat;

    @Column(name = "reponse")
    private String reponse;

    @Column(name = "date_interaction")
    private LocalDateTime dateInteraction;

    @ManyToOne
    private User user;

    @ManyToOne
    private Client client;
}
