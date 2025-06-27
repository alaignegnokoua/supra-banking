package com.suprabanking.services;

import com.suprabanking.services.dto.AmplitudeDataDTO;

import java.util.List;
import java.util.Optional;

public interface AmplitudeDataService {

    AmplitudeDataDTO saveAmplitudeData(AmplitudeDataDTO amplitudeDataDTO);

    AmplitudeDataDTO updateAmplitudeData(AmplitudeDataDTO amplitudeDataDTO, Long id);

    AmplitudeDataDTO partialUpdateAmplitudeData(AmplitudeDataDTO amplitudeDataDTO, Long id);

    List<AmplitudeDataDTO> findAllAmplitudeData();

    Optional<AmplitudeDataDTO> findOne(Long id);

    void delete(Long id);
}
