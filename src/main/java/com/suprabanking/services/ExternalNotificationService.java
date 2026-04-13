package com.suprabanking.services;

import com.suprabanking.models.Client;

public interface ExternalNotificationService {

    void sendSmsNotification(Client client, String content);

    void sendTelegramNotification(Client client, String content);
}
