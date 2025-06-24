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
@Table(name = "amplitude_data")
public class AmplitudeData implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code_operation")
    private String codeOperation;

    @Column(name = "donnees")
    private String donnees;

    @Column(name = "date_sync")
    private LocalDateTime dateSynchronisation;
}
