package com.suprabanking.web.resources;

import com.suprabanking.services.ProduitFinancierService;
import com.suprabanking.services.dto.ProduitFinancierDTO;
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
@RequestMapping("/api/produits-financiers")
public class ProduitFinancierResource {

    private final ProduitFinancierService produitFinancierService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProduitFinancierDTO save(@RequestBody ProduitFinancierDTO dto) {
        log.debug("REST request to save ProduitFinancier : {}", dto);
        return produitFinancierService.saveProduitFinancier(dto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        log.debug("REST request to get ProduitFinancier by id : {}", id);
        Optional<ProduitFinancierDTO> dto = produitFinancierService.findOne(id);
        return dto.<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("ProduitFinancier not found"));
    }

    @GetMapping
    public List<ProduitFinancierDTO> getAll() {
        log.debug("REST request to get all ProduitFinanciers");
        return produitFinancierService.findAllProduitFinanciers();
    }

    @PutMapping("/{id}")
    public ProduitFinancierDTO update(@PathVariable Long id, @RequestBody ProduitFinancierDTO dto) {
        log.debug("REST request to update ProduitFinancier : {}", dto);
        return produitFinancierService.updateProduitFinancier(dto, id);
    }

    @PatchMapping("/{id}")
    public ProduitFinancierDTO partialUpdate(@PathVariable Long id, @RequestBody ProduitFinancierDTO dto) {
        log.debug("REST request to partial update ProduitFinancier : {}", dto);
        return produitFinancierService.partialUpdateProduitFinancier(dto, id);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        log.debug("REST request to delete ProduitFinancier : {}", id);
        produitFinancierService.delete(id);
    }
}
