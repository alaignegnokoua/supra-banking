package com.suprabanking.services.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AmplitudeDataDTO implements Serializable {

    private Long id;
    private String codeOperation;
    private String donnees;
    private LocalDateTime dateSynchronisation;
}
