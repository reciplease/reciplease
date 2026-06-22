package org.reciplease.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

/**
 * Production security: validates Reciplease's own HS256-signed JWT bearer tokens (see
 * {@link ReciplaseJwtService}); the allowlist (and any other per-endpoint authorization) is
 * enforced via {@code @PreAuthorize} on individual controller methods rather than URL-matcher
 * rules here, so this filter chain only sets up the JWT mechanics and leaves
 * {@code authorizeHttpRequests} fully open except for {@code /api/auth/exchange}, which is how
 * a caller obtains a Reciplease JWT in the first place and so must be reachable without one —
 * it instead authenticates the caller via the {@code X-Internal-Secret} header itself.
 * <p>
 * Missing/invalid tokens still yield 401 (the resource server rejects malformed bearer
 * tokens before a request reaches a controller); a valid token whose user id is not on the
 * allowlist gets no {@code ROLE_RECIPLEASE} authority, so {@code @PreAuthorize} checks against
 * it yield 403.
 */
@Configuration(proxyBeanMethods = false)
@EnableWebSecurity
@EnableMethodSecurity
@Profile("cloud")
@RequiredArgsConstructor
public class CloudWebSecurityConfig {

    private final AllowlistJwtAuthenticationConverter allowlistJwtAuthenticationConverter;

    @Value("${reciplease.jwt.signing-secret}")
    private String signingSecret;

    @Bean
    public JwtDecoder jwtDecoder() {
        final var key = new SecretKeySpec(signingSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        return NimbusJwtDecoder.withSecretKey(key).build();
    }

    @Bean
    public SecurityFilterChain filterChain(final HttpSecurity http) throws Exception {
        return http
                .sessionManagement(session -> session.sessionCreationPolicy(STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/exchange").permitAll()
                        .anyRequest().permitAll())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .decoder(jwtDecoder())
                                .jwtAuthenticationConverter(allowlistJwtAuthenticationConverter)))
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .headers(headers -> headers.frameOptions(frame -> frame.disable()))
                .build();
    }
}
