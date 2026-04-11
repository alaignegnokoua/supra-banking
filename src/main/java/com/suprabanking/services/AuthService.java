package com.suprabanking.services;

import com.suprabanking.config.security.JwtService;
import com.suprabanking.models.Client;
import com.suprabanking.models.Role;
import com.suprabanking.models.User;
import com.suprabanking.repositories.ClientRepository;
import com.suprabanking.repositories.RoleRepository;
import com.suprabanking.repositories.UserRepository;
import com.suprabanking.services.dto.auth.AuthResponse;
import com.suprabanking.services.dto.auth.CurrentUserResponse;
import com.suprabanking.services.dto.auth.LoginRequest;
import com.suprabanking.services.dto.auth.RegisterRequest;
import com.suprabanking.services.dto.auth.UpdateProfileRequest;
import com.suprabanking.web.errors.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Ce username est déjà utilisé");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Cet email est déjà utilisé");
        }

        Role clientRole = roleRepository.findByNom("ROLE_CLIENT")
                .orElseThrow(() -> new ResourceNotFoundException("ROLE_CLIENT introuvable"));

        Client client = new Client();
        client.setNom(request.getUsername());
        client.setPrenom("Client");
        client.setEmail(request.getEmail());
        client.setTelephone("N/A");
        client.setIdentifiant(request.getUsername());
        client.setMotDePasse(passwordEncoder.encode(request.getPassword()));
        client = clientRepository.save(client);

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setMotDePasse(passwordEncoder.encode(request.getPassword()));
        user.setEnabled(true);
        user.setRoles(Set.of(clientRole));
        user.setClient(client);

        User savedUser = userRepository.save(user);
        return buildAuthResponse(savedUser);
    }

    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
        } catch (Exception e) {
            throw new BadCredentialsException("Identifiants invalides");
        }

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));

        return buildAuthResponse(user);
    }

    public CurrentUserResponse getCurrentUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));

        Client client = user.getClient();

        return CurrentUserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .enabled(user.isEnabled())
                .roles(user.getRoles().stream().map(Role::getNom).toList())
            .clientId(client != null ? client.getId() : null)
            .clientNom(client != null ? client.getNom() : null)
            .clientPrenom(client != null ? client.getPrenom() : null)
            .clientEmail(client != null ? client.getEmail() : null)
            .clientTelephone(client != null ? client.getTelephone() : null)
                .build();
    }

    public CurrentUserResponse updateCurrentUserProfile(String username, UpdateProfileRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));

        Client client = user.getClient();
        if (client == null) {
            throw new ResourceNotFoundException("Profil client introuvable");
        }

        String newEmail = request.getEmail().trim();
        String currentEmail = user.getEmail();

        if (currentEmail == null || !currentEmail.equalsIgnoreCase(newEmail)) {
            if (userRepository.existsByEmail(newEmail)) {
                throw new IllegalArgumentException("Cet email est déjà utilisé");
            }
        }

        user.setEmail(newEmail);
        client.setEmail(newEmail);
        client.setNom(request.getNom().trim());
        client.setPrenom(request.getPrenom().trim());
        client.setTelephone(request.getTelephone() != null ? request.getTelephone().trim() : null);

        clientRepository.save(client);
        userRepository.save(user);

        return getCurrentUser(username);
    }

    private AuthResponse buildAuthResponse(User user) {
        List<String> roleNames = user.getRoles().stream()
                .map(Role::getNom)
                .toList();

        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("roles", roleNames);

        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getMotDePasse())
                .authorities(roleNames.toArray(new String[0]))
                .disabled(!user.isEnabled())
                .build();

        String token = jwtService.generateToken(userDetails, extraClaims);

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpirationMs())
                .username(user.getUsername())
                .roles(roleNames)
                .build();
    }
}
