package com.suprabanking.config.security;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomUserDetailsService customUserDetailsService;

    @Value("#{'${app.cors.allowed-origins}'.split(',')}")
    private List<String> allowedOrigins;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/api-docs/**", "/v3/api-docs/**").permitAll()
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/api/auth/login", "/api/auth/register").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/auth/me").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/auth/me/profile").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/auth/me/password").authenticated()

                .requestMatchers(HttpMethod.GET, "/api/amplitude-data/**").hasAnyRole("ADMIN", "AGENT")
                .requestMatchers(HttpMethod.POST, "/api/amplitude-data/**").hasAnyRole("ADMIN", "AGENT")
                .requestMatchers(HttpMethod.PUT, "/api/amplitude-data/**").hasAnyRole("ADMIN", "AGENT")
                .requestMatchers(HttpMethod.PATCH, "/api/amplitude-data/**").hasAnyRole("ADMIN", "AGENT")
                .requestMatchers(HttpMethod.DELETE, "/api/amplitude-data/**").hasAnyRole("ADMIN", "AGENT")

                .requestMatchers(HttpMethod.GET, "/api/produits-financiers/**").hasAnyRole("ADMIN", "AGENT", "CLIENT")
                .requestMatchers(HttpMethod.POST, "/api/produits-financiers/**").hasAnyRole("ADMIN", "AGENT")
                .requestMatchers(HttpMethod.PUT, "/api/produits-financiers/**").hasAnyRole("ADMIN", "AGENT")
                .requestMatchers(HttpMethod.PATCH, "/api/produits-financiers/**").hasAnyRole("ADMIN", "AGENT")
                .requestMatchers(HttpMethod.DELETE, "/api/produits-financiers/**").hasAnyRole("ADMIN", "AGENT")

                        .requestMatchers(HttpMethod.GET, "/api/clients/**").hasAnyRole("ADMIN", "AGENT")
                        .requestMatchers(HttpMethod.POST, "/api/clients/**").hasAnyRole("ADMIN", "AGENT")
                        .requestMatchers(HttpMethod.PUT, "/api/clients/**").hasAnyRole("ADMIN", "AGENT")
                        .requestMatchers(HttpMethod.PATCH, "/api/clients/**").hasAnyRole("ADMIN", "AGENT")
                        .requestMatchers(HttpMethod.DELETE, "/api/clients/**").hasAnyRole("ADMIN")

                        .requestMatchers(HttpMethod.GET, "/api/comptes/**").hasAnyRole("ADMIN", "AGENT", "CLIENT")
                        .requestMatchers(HttpMethod.POST, "/api/comptes/**").hasAnyRole("ADMIN", "AGENT")
                        .requestMatchers(HttpMethod.PUT, "/api/comptes/**").hasAnyRole("ADMIN", "AGENT")
                        .requestMatchers(HttpMethod.PATCH, "/api/comptes/**").hasAnyRole("ADMIN", "AGENT")
                        .requestMatchers(HttpMethod.DELETE, "/api/comptes/**").hasAnyRole("ADMIN", "AGENT")

                        .requestMatchers(HttpMethod.GET, "/api/beneficiaires/me/**").hasRole("CLIENT")
                        .requestMatchers(HttpMethod.POST, "/api/beneficiaires/me/**").hasRole("CLIENT")
                        .requestMatchers(HttpMethod.PUT, "/api/beneficiaires/me/**").hasRole("CLIENT")
                        .requestMatchers(HttpMethod.PATCH, "/api/beneficiaires/me/**").hasRole("CLIENT")
                        .requestMatchers(HttpMethod.DELETE, "/api/beneficiaires/me/**").hasRole("CLIENT")

                        .requestMatchers(HttpMethod.GET, "/api/notifications/me/**").hasRole("CLIENT")
                        .requestMatchers(HttpMethod.PATCH, "/api/notifications/me/**").hasRole("CLIENT")

                        .requestMatchers(HttpMethod.GET, "/api/transactions/**").hasAnyRole("ADMIN", "AGENT", "CLIENT")
                        .requestMatchers(HttpMethod.POST, "/api/transactions/me/**").hasRole("CLIENT")
                        .requestMatchers(HttpMethod.POST, "/api/transactions/**").hasAnyRole("ADMIN", "AGENT")
                        .requestMatchers(HttpMethod.PUT, "/api/transactions/**").hasAnyRole("ADMIN", "AGENT")
                        .requestMatchers(HttpMethod.PATCH, "/api/transactions/**").hasAnyRole("ADMIN", "AGENT")
                        .requestMatchers(HttpMethod.DELETE, "/api/transactions/**").hasAnyRole("ADMIN", "AGENT")

                        .anyRequest().authenticated())
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(customUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(allowedOrigins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "Origin"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
