package com.suprabanking.services.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CompteDTO implements Serializable {

    private Long id;

    @NotBlank(message = "Le numéro de compte est obligatoire")
    private String numeroCompte;

    @NotBlank(message = "Le type est obligatoire")
    private String type;

    @NotNull(message = "Le solde est obligatoire")
    @PositiveOrZero(message = "Le solde doit être positif")
    private Double solde;

    private LocalDateTime dateCreation;

    @NotNull(message = "Le clientId est obligatoire")
    private Long clientId;
}
