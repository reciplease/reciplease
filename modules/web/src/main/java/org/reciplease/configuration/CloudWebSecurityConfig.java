package org.reciplease.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

/**
 * Production security: validates Google-issued JWT bearer tokens; the allowlist (and any
 * other per-endpoint authorization) is enforced via {@code @PreAuthorize} on individual
 * controller methods rather than URL-matcher rules here, so this filter chain only sets up
 * the JWT mechanics and leaves {@code authorizeHttpRequests} fully open. A method with no
 * {@code @PreAuthorize} annotation is therefore public by default — every controller method
 * must be consciously annotated, or consciously left public.
 * <p>
 * Missing/invalid tokens still yield 401 (the resource server rejects malformed bearer
 * tokens before a request reaches a controller); a valid token whose verified email is not
 * on the allowlist gets no {@code ROLE_RECIPLEASE} authority, so {@code @PreAuthorize}
 * checks against it yield 403.
 */
@Configuration(proxyBeanMethods = false)
@EnableWebSecurity
@EnableMethodSecurity
@Profile("cloud")
@RequiredArgsConstructor
public class CloudWebSecurityConfig {

    private final AllowlistJwtAuthenticationConverter allowlistJwtAuthenticationConverter;

    @Bean
    public SecurityFilterChain filterChain(final HttpSecurity http) throws Exception {
        return http
                .sessionManagement(session -> session.sessionCreationPolicy(STATELESS))
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt ->
                        jwt.jwtAuthenticationConverter(allowlistJwtAuthenticationConverter)))
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .headers(headers -> headers.frameOptions(frame -> frame.disable()))
                .build();
    }
}
