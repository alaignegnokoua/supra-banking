package com.suprabanking.config;

import com.suprabanking.models.Role;
import com.suprabanking.models.User;
import com.suprabanking.repositories.RoleRepository;
import com.suprabanking.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@Configuration
@RequiredArgsConstructor
public class InitRoleConfig {

    @Value("${app.security.admin.username}")
    private String adminUsername;

    @Value("${app.security.admin.email}")
    private String adminEmail;

    @Value("${app.security.admin.password}")
    private String adminPassword;

    private final PasswordEncoder passwordEncoder;

    @Bean
    CommandLineRunner initRoles(RoleRepository roleRepository, UserRepository userRepository) {
        return args -> {
            if (roleRepository.findByNom("ROLE_ADMIN").isEmpty()) {
                roleRepository.save(new Role(null, "ROLE_ADMIN"));
            }
            if (roleRepository.findByNom("ROLE_AGENT").isEmpty()) {
                roleRepository.save(new Role(null, "ROLE_AGENT"));
            }
            if (roleRepository.findByNom("ROLE_CLIENT").isEmpty()) {
                roleRepository.save(new Role(null, "ROLE_CLIENT"));
            }

            if (userRepository.findByUsername(adminUsername).isEmpty()) {
                Role adminRole = roleRepository.findByNom("ROLE_ADMIN")
                        .orElseThrow(() -> new IllegalStateException("ROLE_ADMIN introuvable"));

                User admin = new User();
                admin.setUsername(adminUsername);
                admin.setEmail(adminEmail);
                admin.setMotDePasse(passwordEncoder.encode(adminPassword));
                admin.setEnabled(true);
                admin.setRoles(Set.of(adminRole));

                userRepository.save(admin);
            }
        };
    }
}
