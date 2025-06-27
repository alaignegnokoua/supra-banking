package com.suprabanking.config;

import com.suprabanking.models.Role;
import com.suprabanking.repositories.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InitRoleConfig {

    @Bean
    CommandLineRunner initRoles(RoleRepository roleRepository) {
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
        };
    }
}
