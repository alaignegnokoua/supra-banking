package com.suprabanking.services;

import com.suprabanking.config.security.JwtService;
import com.suprabanking.models.Role;
import com.suprabanking.models.User;
import com.suprabanking.repositories.RoleRepository;
import com.suprabanking.repositories.UserRepository;
import com.suprabanking.services.dto.auth.AuthResponse;
import com.suprabanking.services.dto.auth.LoginRequest;
import com.suprabanking.services.dto.auth.RegisterRequest;
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

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setMotDePasse(passwordEncoder.encode(request.getPassword()));
        user.setEnabled(true);
        user.setRoles(Set.of(clientRole));

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
