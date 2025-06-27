package com.suprabanking.services.mapper.impl;

import com.suprabanking.models.AmplitudeData;
import com.suprabanking.services.dto.AmplitudeDataDTO;
import com.suprabanking.services.mapper.AmplitudeDataMapper;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AmplitudeDataMapperImpl implements AmplitudeDataMapper {

    private final ModelMapper modelMapper;

    @Override
    public AmplitudeData toEntity(AmplitudeDataDTO dto) {
        return modelMapper.map(dto, AmplitudeData.class);
    }

    @Override
    public AmplitudeDataDTO toDto(AmplitudeData entity) {
        return modelMapper.map(entity, AmplitudeDataDTO.class);
    }

    @Override
    public void partialUpdate(AmplitudeData entity, AmplitudeDataDTO dto) {
        modelMapper.map(dto, entity);
    }
}
