package org.reciplease.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

/**
 * Production security: validates Google-issued JWT bearer tokens and enforces the allowlist.
 * <p>
 * The {@code /api/**} endpoints require a token whose issuer and audience match the configured
 * {@code spring.security.oauth2.resourceserver.jwt.*} values (Google's issuer and our OAuth
 * client id). A missing/invalid token yields 401; a valid token whose verified email is not on
 * the allowlist yields 403, which the frontend surfaces as a "not allowed" message.
 */
@Configuration(proxyBeanMethods = false)
@EnableWebSecurity
@Profile("cloud")
@RequiredArgsConstructor
public class CloudWebSecurityConfig {

    private final AllowlistJwtAuthenticationConverter allowlistJwtAuthenticationConverter;

    @Bean
    public SecurityFilterChain filterChain(final HttpSecurity http) throws Exception {
        return http
                .sessionManagement(session -> session.sessionCreationPolicy(STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Swagger UI / OpenAPI docs remain public.
                        .requestMatchers("/", "/openapi/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        // Recipes and measures are publicly readable.
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/recipes", "/api/recipes/**", "/api/measures", "/api/measures/**").permitAll()
                        // All other API endpoints require an allowlisted Google user.
                        .requestMatchers("/api/**").hasRole("RECIPLEASE")
                        .anyRequest().permitAll())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt ->
                        jwt.jwtAuthenticationConverter(allowlistJwtAuthenticationConverter)))
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .headers(headers -> headers.frameOptions(frame -> frame.disable()))
                .build();
    }
}
