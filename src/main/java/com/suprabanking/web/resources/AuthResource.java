package com.suprabanking.web.resources;

import com.suprabanking.services.AuthService;
import com.suprabanking.services.dto.auth.AuthResponse;
import com.suprabanking.services.dto.auth.ChangePasswordRequest;
import com.suprabanking.services.dto.auth.CurrentUserResponse;
import com.suprabanking.services.dto.auth.LoginRequest;
import com.suprabanking.services.dto.auth.RegisterRequest;
import com.suprabanking.services.dto.auth.UpdateProfileRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;

import java.security.Principal;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthResource {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.debug("REST request to register user: {}", request.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.debug("REST request to login user: {}", request.getUsername());
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/me")
    public ResponseEntity<CurrentUserResponse> me(Principal principal) {
        return ResponseEntity.ok(authService.getCurrentUser(principal.getName()));
    }

    @PutMapping("/me/profile")
    public ResponseEntity<CurrentUserResponse> updateProfile(Principal principal,
                                                             @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(authService.updateCurrentUserProfile(principal.getName(), request));
    }

    @PutMapping("/me/password")
    public ResponseEntity<Void> changePassword(Principal principal,
                                               @Valid @RequestBody ChangePasswordRequest request) {
        authService.changeCurrentUserPassword(principal.getName(), request);
        return ResponseEntity.noContent().build();
    }
}
