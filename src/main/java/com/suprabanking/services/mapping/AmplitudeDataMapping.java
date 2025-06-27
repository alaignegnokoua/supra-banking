package com.suprabanking.services.mapping;

import com.suprabanking.models.AmplitudeData;
import com.suprabanking.services.dto.AmplitudeDataDTO;

public final class AmplitudeDataMapping {

    private AmplitudeDataMapping() {}

    public static void partialUpdate(AmplitudeData entity, AmplitudeDataDTO dto) {
        if(dto.getCodeOperation() != null){
            entity.setCodeOperation(dto.getCodeOperation());
        }
        if(dto.getDonnees() != null){
            entity.setDonnees(dto.getDonnees());
        }
        if(dto.getDateSynchronisation() != null){
            entity.setDateSynchronisation(dto.getDateSynchronisation());
        }
    }
}
