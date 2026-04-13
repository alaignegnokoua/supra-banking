package com.suprabanking.services.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateClientTransferLimitsRequest implements Serializable {

    private Double maxSingleTransferAmount;
    private Double maxDailyTransferTotal;
    private Integer maxDailyTransferCount;
    private Integer minTransferIntervalSeconds;
}
