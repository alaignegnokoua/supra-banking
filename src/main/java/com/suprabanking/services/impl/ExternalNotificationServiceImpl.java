package com.suprabanking.services.impl;

import com.suprabanking.models.Client;
import com.suprabanking.services.ExternalNotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ExternalNotificationServiceImpl implements ExternalNotificationService {

    @Value("${app.notifications.sms.enabled:false}")
    private boolean smsEnabled;

    @Value("${app.notifications.telegram.enabled:false}")
    private boolean telegramEnabled;

    @Override
    public void sendSmsNotification(Client client, String content) {
        if (!smsEnabled || client == null || client.getTelephone() == null || client.getTelephone().isBlank()) {
            return;
        }

        log.info("SMS notification simulated for clientId={} phone={} content={}",
                client.getId(), client.getTelephone(), content);
    }

    @Override
    public void sendTelegramNotification(Client client, String content) {
        if (!telegramEnabled || client == null || client.getTelegramChatId() == null || client.getTelegramChatId().isBlank()) {
            return;
        }

        log.info("Telegram notification simulated for clientId={} chatId={} content={}",
                client.getId(), client.getTelegramChatId(), content);
    }
}
