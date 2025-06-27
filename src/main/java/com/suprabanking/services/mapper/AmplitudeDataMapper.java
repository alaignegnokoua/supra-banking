package com.suprabanking.services.mapper;

import com.suprabanking.models.AmplitudeData;
import com.suprabanking.services.dto.AmplitudeDataDTO;

public interface AmplitudeDataMapper {

    AmplitudeData toEntity(AmplitudeDataDTO dto);

    AmplitudeDataDTO toDto(AmplitudeData entity);

    void partialUpdate(AmplitudeData entity, AmplitudeDataDTO dto);
}
