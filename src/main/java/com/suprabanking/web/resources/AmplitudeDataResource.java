package com.suprabanking.web.resources;

import com.suprabanking.services.AmplitudeDataService;
import com.suprabanking.services.dto.AmplitudeDataDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/amplitude-data")
public class AmplitudeDataResource {

    private final AmplitudeDataService amplitudeDataService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AmplitudeDataDTO save(@RequestBody AmplitudeDataDTO dto) {
        log.debug("REST request to save AmplitudeData : {}", dto);
        return amplitudeDataService.saveAmplitudeData(dto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        log.debug("REST request to get AmplitudeData by id : {}", id);
        Optional<AmplitudeDataDTO> dto = amplitudeDataService.findOne(id);
        return dto.<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("AmplitudeData not found"));
    }

    @GetMapping
    public List<AmplitudeDataDTO> getAll() {
        log.debug("REST request to get all AmplitudeData");
        return amplitudeDataService.findAllAmplitudeData();
    }

    @PutMapping("/{id}")
    public AmplitudeDataDTO update(@PathVariable Long id, @RequestBody AmplitudeDataDTO dto) {
        log.debug("REST request to update AmplitudeData : {}", dto);
        return amplitudeDataService.updateAmplitudeData(dto, id);
    }

    @PatchMapping("/{id}")
    public AmplitudeDataDTO partialUpdate(@PathVariable Long id, @RequestBody AmplitudeDataDTO dto) {
        log.debug("REST request to partial update AmplitudeData : {}", dto);
        return amplitudeDataService.partialUpdateAmplitudeData(dto, id);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        log.debug("REST request to delete AmplitudeData : {}", id);
        amplitudeDataService.delete(id);
    }
}
