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
    private String operationType;
    private String riskProfile;
    private Integer blockThreshold;
    private Double amountRatio;
    private Double dailyAmountRatio;
    private Double dailyCountRatio;
    private Integer amountScore;
    private Integer dailyAmountScore;
    private Integer dailyCountScore;
    private Boolean newBeneficiary;
    private Integer newBeneficiaryScore;
    private Boolean unusualHour;
    private Integer unusualHourScore;
    private Boolean multiBeneficiaryVelocity;
    private Integer multiBeneficiaryVelocityScore;
    private Integer distinctBeneficiariesWindow;
    private Integer externalTransfersWindow;
    private Boolean unusualAmount;
    private Integer unusualAmountScore;
    private Double historicalAverageAmount;
    private Boolean repeatedSmallTransfers;
    private Integer repeatedSmallTransfersScore;
    private Integer smallTransfersWindowCount;
}
