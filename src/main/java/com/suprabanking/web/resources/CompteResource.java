package com.suprabanking.web.resources;

import com.suprabanking.services.CompteService;
import com.suprabanking.services.dto.CompteDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/comptes")
public class CompteResource {

    private final CompteService compteService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompteDTO save(@Valid @RequestBody CompteDTO dto) {
        log.debug("REST request to save Compte : {}", dto);
        return compteService.saveCompte(dto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompteDTO> getById(@PathVariable Long id) {
        log.debug("REST request to get Compte by id : {}", id);
        return compteService.findOne(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public Page<CompteDTO> getAll(Pageable pageable) {
        log.debug("REST request to get all Comptes");
        return compteService.findAllComptes(pageable);
    }

    @PutMapping("/{id}")
    public CompteDTO update(@PathVariable Long id, @Valid @RequestBody CompteDTO dto) {
        log.debug("REST request to update Compte : {}", dto);
        return compteService.updateCompte(dto, id);
    }

    @PatchMapping("/{id}")
    public CompteDTO partialUpdate(@PathVariable Long id, @RequestBody CompteDTO dto) {
        log.debug("REST request to partial update Compte : {}", dto);
        return compteService.partialUpdateCompte(dto, id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        log.debug("REST request to delete Compte : {}", id);
        compteService.delete(id);
    }
}
