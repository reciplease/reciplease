package org.reciplease.configuration;

import lombok.RequiredArgsConstructor;
import org.reciplease.service.ApiKeyService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

/**
 * Production security: validates Reciplease's own HS256-signed JWT bearer tokens (see
 * {@link ReciplaseJwtService}), resolved from the {@code reciplease-session} cookie or, failing
 * that, the {@code Authorization} header (see {@link CookieBearerTokenResolver}); authorization
 * (and any other per-endpoint check) is enforced via {@code @PreAuthorize} on individual
 * controller methods rather than URL-matcher rules here.
 * <p>
 * {@link ApiKeyAuthenticationFilter} runs ahead of the JWT-based {@code oauth2ResourceServer}
 * filter to authenticate house service-account API keys instead, which aren't JWTs at all.
 * <p>
 * Missing/invalid tokens still yield 401 (the resource server rejects malformed bearer
 * tokens before a request reaches a controller); a valid token whose user belongs to no house
 * gets no {@code ROLE_RECIPLEASE} authority (see {@link MembershipJwtAuthenticationConverter}),
 * so {@code @PreAuthorize} checks against it yield 403.
 * <p>
 * That "invalid token yields 401 before authorization runs" behaviour is exactly why the truly
 * anonymous endpoints — {@code /api/auth/exchange} (how a caller obtains a Reciplease JWT in the
 * first place, authenticated via the {@code X-Internal-Secret} header instead) and the passkey
 * signup/login ceremonies (which must work with no session at all) — get their own filter chain
 * ({@link #anonymousFilterChain}) with no {@code oauth2ResourceServer} configured, rather than
 * just a {@code permitAll()} rule in the main one: a stale/expired {@code reciplease-session}
 * cookie forwarded alongside an otherwise-anonymous request (e.g. by a signed-out user with a
 * lingering expired session) must not be able to 401 it — {@code BearerTokenAuthenticationFilter}
 * runs ahead of {@code authorizeHttpRequests}, so {@code permitAll()} alone doesn't help.
 */
@Configuration(proxyBeanMethods = false)
@EnableWebSecurity
@EnableMethodSecurity
@Profile("cloud")
@RequiredArgsConstructor
public class CloudWebSecurityConfig {

    private final MembershipJwtAuthenticationConverter membershipJwtAuthenticationConverter;
    private final ApiKeyService apiKeyService;

    @Value("${reciplease.jwt.signing-secret}")
    private String signingSecret;

    @Bean
    public JwtDecoder jwtDecoder() {
        final var key = new SecretKeySpec(signingSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        return NimbusJwtDecoder.withSecretKey(key).build();
    }

    @Bean
    @Order(1)
    public SecurityFilterChain anonymousFilterChain(final HttpSecurity http) throws Exception {
        return http
                .securityMatcher("/api/auth/exchange", "/api/passkey/signup/**", "/api/passkey/login/**")
                .sessionManagement(session -> session.sessionCreationPolicy(STATELESS))
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .headers(headers -> headers.frameOptions(frame -> frame.disable()))
                .build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain filterChain(final HttpSecurity http) throws Exception {
        return http
                .sessionManagement(session -> session.sessionCreationPolicy(STATELESS))
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .bearerTokenResolver(new CookieBearerTokenResolver())
                        .jwt(jwt -> jwt
                                .decoder(jwtDecoder())
                                .jwtAuthenticationConverter(membershipJwtAuthenticationConverter)))
                .addFilterBefore(new ApiKeyAuthenticationFilter(apiKeyService), BearerTokenAuthenticationFilter.class)
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .headers(headers -> headers.frameOptions(frame -> frame.disable()))
                .build();
    }
}
