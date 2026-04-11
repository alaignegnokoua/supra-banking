package com.suprabanking.services.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CurrentUserResponse {

    private Long id;
    private String username;
    private String email;
    private boolean enabled;
    private List<String> roles;

    private Long clientId;
    private String clientNom;
    private String clientPrenom;
    private String clientEmail;
    private String clientTelephone;
    private boolean notificationsInAppEnabled;
    private boolean notificationsEmailEnabled;
}
