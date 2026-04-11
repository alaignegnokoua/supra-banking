package com.suprabanking.services.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransferRiskAssessmentDTO implements Serializable {

    private Integer score;
    private String level;
    private Boolean blocked;
    private String message;
}
