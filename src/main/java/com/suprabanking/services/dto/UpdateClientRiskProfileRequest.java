package com.suprabanking.services.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateClientRiskProfileRequest implements Serializable {

    @NotBlank(message = "Le profil de risque est obligatoire")
    private String riskProfile;
}
