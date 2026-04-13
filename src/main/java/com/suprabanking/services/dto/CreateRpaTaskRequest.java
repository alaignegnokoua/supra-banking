package com.suprabanking.services.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateRpaTaskRequest {

    @NotBlank(message = "Le nom de la tâche est obligatoire")
    private String nomTache;

    @NotBlank(message = "Le type de tâche est obligatoire")
    private String type;

    private String payload;
}
