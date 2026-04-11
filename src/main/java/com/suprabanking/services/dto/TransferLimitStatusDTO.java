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
public class TransferLimitStatusDTO implements Serializable {

    private Double maxSingleAmount;
    private Double maxDailyTotal;
    private Double todayOutgoingTotal;
    private Double remainingDailyAmount;
}
