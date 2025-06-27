package com.suprabanking.services.impl;

import com.suprabanking.models.AmplitudeData;
import com.suprabanking.repositories.AmplitudeDataRepository;
import com.suprabanking.services.AmplitudeDataService;
import com.suprabanking.services.dto.AmplitudeDataDTO;
import com.suprabanking.services.mapper.AmplitudeDataMapper;
import com.suprabanking.services.mapping.AmplitudeDataMapping;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
@Service
public class AmplitudeDataServiceImpl implements AmplitudeDataService {

    private final AmplitudeDataRepository amplitudeDataRepository;
    private final AmplitudeDataMapper amplitudeDataMapper;

    @Override
    public AmplitudeDataDTO saveAmplitudeData(AmplitudeDataDTO dto) {
        log.debug("Request to save AmplitudeData : {}", dto);
        AmplitudeData entity = amplitudeDataMapper.toEntity(dto);
        entity = amplitudeDataRepository.save(entity);
        return amplitudeDataMapper.toDto(entity);
    }

    @Override
    public AmplitudeDataDTO updateAmplitudeData(AmplitudeDataDTO dto, Long id) {
        log.debug("Request to update AmplitudeData : {}", dto);
        AmplitudeData entity = amplitudeDataRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("AmplitudeData not found"));
        dto.setId(id);
        amplitudeDataMapper.partialUpdate(entity, dto);
        entity = amplitudeDataRepository.save(entity);
        return amplitudeDataMapper.toDto(entity);
    }

    @Override
    public AmplitudeDataDTO partialUpdateAmplitudeData(AmplitudeDataDTO dto, Long id) {
        log.debug("Request to partial update AmplitudeData : {}", dto);
        return amplitudeDataRepository.findById(id)
                .map(existing -> {
                    amplitudeDataMapper.partialUpdate(existing, dto);
                    return amplitudeDataMapper.toDto(amplitudeDataRepository.save(existing));
                })
                .orElseThrow(() -> new IllegalArgumentException("AmplitudeData not found"));
    }

    @Override
    public List<AmplitudeDataDTO> findAllAmplitudeData() {
        log.debug("Request to get all AmplitudeData");
        return amplitudeDataRepository.findAll()
                .stream()
                .map(amplitudeDataMapper::toDto)
                .toList();
    }

    @Override
    public Optional<AmplitudeDataDTO> findOne(Long id) {
        log.debug("Request to get AmplitudeData : {}", id);
        return amplitudeDataRepository.findById(id)
                .map(amplitudeDataMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete AmplitudeData : {}", id);
        amplitudeDataRepository.deleteById(id);
    }
}
