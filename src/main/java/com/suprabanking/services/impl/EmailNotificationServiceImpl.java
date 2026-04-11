package com.suprabanking.services.impl;

import com.suprabanking.models.Client;
import com.suprabanking.services.EmailNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailNotificationServiceImpl implements EmailNotificationService {

    private final JavaMailSender mailSender;

    @Value("${app.notifications.email.enabled:false}")
    private boolean emailEnabled;

    @Value("${app.notifications.email.from:no-reply@supra-banking.local}")
    private String fromAddress;

    @Override
    public void sendNotificationEmail(Client client, String contenu) {
        if (!emailEnabled) {
            return;
        }

        if (client == null || client.getEmail() == null || client.getEmail().isBlank()) {
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(client.getEmail());
            message.setSubject("SupraBanking - Nouvelle notification");
            message.setText(contenu);
            mailSender.send(message);
        } catch (Exception ex) {
            log.warn("Failed to send notification email to client {}: {}", client.getId(), ex.getMessage());
        }
    }
}
