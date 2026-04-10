package com.suprabanking.config.security;

import com.suprabanking.models.Role;
import com.suprabanking.models.User;
import com.suprabanking.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur introuvable: " + username));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getMotDePasse())
                .authorities(mapAuthorities(user))
                .disabled(!user.isEnabled())
                .build();
    }

    private Collection<? extends GrantedAuthority> mapAuthorities(User user) {
        return user.getRoles().stream()
                .map(Role::getNom)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());
    }
}
