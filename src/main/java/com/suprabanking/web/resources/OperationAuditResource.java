package com.suprabanking.web.resources;

import com.suprabanking.services.OperationAuditService;
import com.suprabanking.services.dto.OperationAuditDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/audits")
public class OperationAuditResource {

    private final OperationAuditService operationAuditService;

    @GetMapping("/me")
    public List<OperationAuditDTO> getMyAudits() {
        log.debug("REST request to get current client audits");
        return operationAuditService.findMyAudits();
    }

    @GetMapping("/me/page")
    public Page<OperationAuditDTO> getMyAuditsPage(Pageable pageable) {
        log.debug("REST request to get current client audits page: {}", pageable);
        return operationAuditService.findMyAudits(pageable);
    }
}
