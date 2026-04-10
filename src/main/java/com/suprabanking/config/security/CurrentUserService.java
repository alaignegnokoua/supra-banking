package com.suprabanking.config.security;

import com.suprabanking.models.User;
import com.suprabanking.repositories.UserRepository;
import com.suprabanking.web.errors.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final UserRepository userRepository;

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new ResourceNotFoundException("Utilisateur connecté introuvable");
        }
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur connecté introuvable"));
    }

    public boolean isCurrentUserClient() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_CLIENT".equals(a.getAuthority()));
    }

    public Long requireCurrentClientId() {
        User user = getCurrentUser();
        if (user.getClient() == null || user.getClient().getId() == null) {
            throw new ResourceNotFoundException("Profil client introuvable pour l'utilisateur connecté");
        }
        return user.getClient().getId();
    }
}
