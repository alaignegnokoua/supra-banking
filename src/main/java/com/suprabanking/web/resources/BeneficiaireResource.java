package com.suprabanking.web.resources;

import com.suprabanking.services.BeneficiaireService;
import com.suprabanking.services.dto.BeneficiaireDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/beneficiaires")
public class BeneficiaireResource {

    private final BeneficiaireService beneficiaireService;

    @PostMapping("/me")
    @ResponseStatus(HttpStatus.CREATED)
    public BeneficiaireDTO createMyBeneficiaire(@Valid @RequestBody BeneficiaireDTO dto) {
        log.debug("REST request to create my beneficiaire: {}", dto);
        return beneficiaireService.saveMyBeneficiaire(dto);
    }

    @PutMapping("/me/{id}")
    public BeneficiaireDTO updateMyBeneficiaire(@PathVariable Long id, @Valid @RequestBody BeneficiaireDTO dto) {
        log.debug("REST request to update my beneficiaire: {}", id);
        return beneficiaireService.updateMyBeneficiaire(id, dto);
    }

    @GetMapping("/me")
    public List<BeneficiaireDTO> getMyBeneficiaires() {
        log.debug("REST request to get my beneficiaires");
        return beneficiaireService.findMyBeneficiaires();
    }

    @GetMapping("/me/{id}")
    public ResponseEntity<BeneficiaireDTO> getMyBeneficiaire(@PathVariable Long id) {
        log.debug("REST request to get my beneficiaire by id: {}", id);
        return beneficiaireService.findMyBeneficiaire(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/me/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMyBeneficiaire(@PathVariable Long id) {
        log.debug("REST request to delete my beneficiaire: {}", id);
        beneficiaireService.deleteMyBeneficiaire(id);
    }
}
