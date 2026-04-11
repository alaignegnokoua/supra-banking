package com.suprabanking.services.impl;

import com.suprabanking.models.Client;
import com.suprabanking.services.EmailNotificationService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

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
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");

            helper.setFrom(fromAddress);
            helper.setTo(client.getEmail());
            helper.setSubject(buildSubject(contenu));
            helper.setText(buildHtmlBody(client, contenu), true);

            mailSender.send(message);
        } catch (Exception ex) {
            log.warn("Failed to send notification email to client {}: {}", client.getId(), ex.getMessage());
        }
    }

    private String buildSubject(String contenu) {
        String normalized = contenu == null ? "" : contenu.toLowerCase();
        if (normalized.contains("virement externe")) {
            return "SupraBanking - Confirmation de virement externe";
        }
        if (normalized.contains("virement interne")) {
            return "SupraBanking - Confirmation de virement interne";
        }
        return "SupraBanking - Nouvelle notification";
    }

    private String buildHtmlBody(Client client, String contenu) {
        String prenom = client.getPrenom() != null && !client.getPrenom().isBlank() ? client.getPrenom() : "Client";
        String safeContenu = HtmlUtils.htmlEscape(contenu == null ? "" : contenu);

        return """
                <html>
                  <body style='font-family: Arial, sans-serif; color: #111827;'>
                    <h2 style='margin-bottom: 8px;'>SupraBanking</h2>
                    <p>Bonjour %s,</p>
                    <p>Vous avez une nouvelle notification :</p>
                    <div style='background:#f3f4f6; padding:12px; border-radius:8px;'>%s</div>
                    <p style='margin-top:16px;'>Merci de votre confiance.</p>
                  </body>
                </html>
                """.formatted(HtmlUtils.htmlEscape(prenom), safeContenu);
    }
}
