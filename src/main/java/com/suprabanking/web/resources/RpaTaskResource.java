package com.suprabanking.web.resources;

import com.suprabanking.services.RpaTaskService;
import com.suprabanking.services.dto.CreateRpaTaskRequest;
import com.suprabanking.services.dto.RpaTaskDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/rpa-tasks")
public class RpaTaskResource {

    private final RpaTaskService rpaTaskService;

    @PostMapping("/me")
    @ResponseStatus(HttpStatus.CREATED)
    public RpaTaskDTO createMyTask(@Valid @RequestBody CreateRpaTaskRequest request) {
        log.debug("REST request to create RPA task for current user");
        return rpaTaskService.createMyTask(request);
    }

    @GetMapping("/me")
    public List<RpaTaskDTO> getMyTasks() {
        log.debug("REST request to get current user RPA tasks");
        return rpaTaskService.getMyTasks();
    }

    @PatchMapping("/me/{taskId}/cancel")
    public ResponseEntity<Void> cancelMyTask(@PathVariable Long taskId) {
        log.debug("REST request to cancel current user RPA task {}", taskId);
        rpaTaskService.cancelMyTask(taskId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/pending")
    public List<RpaTaskDTO> getPendingTasks(Authentication authentication) {
        ensureAdminOrAgent(authentication);
        return rpaTaskService.getPendingTasks();
    }

    @GetMapping
    public List<RpaTaskDTO> getAllTasks(Authentication authentication) {
        ensureAdminOrAgent(authentication);
        return rpaTaskService.getAllTasks();
    }

    @PostMapping("/admin/{taskId}/execute")
    public RpaTaskDTO executeTaskNow(@PathVariable Long taskId, Authentication authentication) {
        ensureAdminOrAgent(authentication);
        return rpaTaskService.executeTaskNow(taskId);
    }

    @PostMapping("/admin/execute-pending")
    public ResponseEntity<Integer> executePendingTasks(Authentication authentication) {
        ensureAdminOrAgent(authentication);
        int executed = rpaTaskService.executePendingTasksBatch();
        return ResponseEntity.ok(executed);
    }

    private void ensureAdminOrAgent(Authentication authentication) {
        boolean allowed = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()) || "ROLE_AGENT".equals(a.getAuthority()));

        if (!allowed) {
            throw new org.springframework.security.access.AccessDeniedException("Accès refusé");
        }
    }
}
