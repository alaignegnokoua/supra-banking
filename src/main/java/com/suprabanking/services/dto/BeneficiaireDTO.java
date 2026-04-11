package com.suprabanking.services.dto;

import jakarta.validation.constraints.Email;
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
public class BeneficiaireDTO implements Serializable {

    private Long id;

    @NotBlank(message = "Le nom du bénéficiaire est obligatoire")
    private String nom;

    private String iban;

    private String rib;

    private String banque;

    @Email(message = "Format d'email invalide")
    private String email;
}
