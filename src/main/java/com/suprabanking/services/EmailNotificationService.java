package com.suprabanking.services;

import com.suprabanking.models.Client;

public interface EmailNotificationService {
    void sendNotificationEmail(Client client, String contenu);
}
