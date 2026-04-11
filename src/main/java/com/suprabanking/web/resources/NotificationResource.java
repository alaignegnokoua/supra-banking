package com.suprabanking.web.resources;

import com.suprabanking.services.NotificationService;
import com.suprabanking.services.dto.NotificationDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationResource {

    private final NotificationService notificationService;

    @GetMapping("/me")
    public List<NotificationDTO> getMyNotifications() {
        log.debug("REST request to get my notifications");
        return notificationService.findMyNotifications();
    }

    @PatchMapping("/me/{id}/read")
    public NotificationDTO markAsRead(@PathVariable Long id) {
        log.debug("REST request to mark notification as read: {}", id);
        return notificationService.markAsRead(id);
    }
}
