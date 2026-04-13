package com.suprabanking.web.resources;

import com.suprabanking.services.ClientService;
import com.suprabanking.services.dto.ClientDTO;
import com.suprabanking.services.dto.UpdateClientRiskProfileRequest;
import com.suprabanking.services.dto.UpdateClientTransferLimitsRequest;
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
@RequestMapping("/api/clients")
public class ClientResource {

    private final ClientService clientService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClientDTO save(@Valid @RequestBody ClientDTO dto) {
        log.debug("REST request to save Client : {}", dto);
        return clientService.saveClient(dto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClientDTO> getById(@PathVariable Long id) {
        log.debug("REST request to get Client by id : {}", id);
        return clientService.findOne(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public Page<ClientDTO> getAll(Pageable pageable) {
        log.debug("REST request to get all Clients");
        return clientService.findAllClients(pageable);
    }

    @PutMapping("/{id}")
    public ClientDTO update(@PathVariable Long id, @Valid @RequestBody ClientDTO dto) {
        log.debug("REST request to update Client : {}", dto);
        return clientService.updateClient(dto, id);
    }

    @PatchMapping("/{id}")
    public ClientDTO partialUpdate(@PathVariable Long id, @RequestBody ClientDTO dto) {
        log.debug("REST request to partial update Client : {}", dto);
        return clientService.partialUpdateClient(dto, id);
    }

    @PatchMapping("/{id}/risk-profile")
    public ClientDTO updateRiskProfile(@PathVariable Long id, @Valid @RequestBody UpdateClientRiskProfileRequest request) {
        log.debug("REST request to update Client risk profile id={} profile={}", id, request.getRiskProfile());
        return clientService.updateClientRiskProfile(id, request.getRiskProfile());
    }

    @PatchMapping("/{id}/transfer-limits")
    public ClientDTO updateTransferLimits(@PathVariable Long id, @RequestBody UpdateClientTransferLimitsRequest request) {
        log.debug("REST request to update Client transfer limits id={}", id);
        return clientService.updateClientTransferLimits(
                id,
                request.getMaxSingleTransferAmount(),
                request.getMaxDailyTransferTotal(),
                request.getMaxDailyTransferCount(),
                request.getMinTransferIntervalSeconds()
        );
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        log.debug("REST request to delete Client : {}", id);
        clientService.delete(id);
    }
}
