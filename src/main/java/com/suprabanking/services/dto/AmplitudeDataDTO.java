package com.suprabanking.services.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AmplitudeDataDTO implements Serializable {

    private Long id;
    @NotBlank(message = "Le code opération est obligatoire")
    private String codeOperation;
    @NotBlank(message = "Les données sont obligatoires")
    private String donnees;
    @NotNull(message = "La date de synchronisation est obligatoire")
    private LocalDateTime dateSynchronisation;
}
