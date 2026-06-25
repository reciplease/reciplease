package org.reciplease.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.webauthn.api.PublicKeyCredentialRpEntity;
import org.springframework.security.web.webauthn.jackson.WebauthnJacksonModule;
import org.springframework.security.web.webauthn.management.PublicKeyCredentialUserEntityRepository;
import org.springframework.security.web.webauthn.management.UserCredentialRepository;
import org.springframework.security.web.webauthn.management.WebAuthnRelyingPartyOperations;
import org.springframework.security.web.webauthn.management.Webauthn4JRelyingPartyOperations;

import java.util.Arrays;
import java.util.Set;

/**
 * Wires up Spring Security's WebAuthn building blocks directly, rather than its {@code .webAuthn()}
 * DSL: that DSL (and its default {@code /webauthn/register}, {@code /login/webauthn} endpoints)
 * assumes session-based, CSRF-protected form login, which this app's stateless bearer-JWT auth
 * doesn't have. {@link org.reciplease.controller.PasskeyController} calls
 * {@link WebAuthnRelyingPartyOperations} directly instead.
 */
@Configuration(proxyBeanMethods = false)
public class PasskeyConfig {

    @Bean
    public PublicKeyCredentialRpEntity passkeyRelyingParty(
            @Value("${reciplease.webauthn.rp-id}") final String rpId,
            @Value("${reciplease.webauthn.rp-name}") final String rpName) {
        return PublicKeyCredentialRpEntity.builder().id(rpId).name(rpName).build();
    }

    @Bean
    public WebAuthnRelyingPartyOperations webAuthnRelyingPartyOperations(
            final PublicKeyCredentialUserEntityRepository userEntities,
            final UserCredentialRepository userCredentials,
            final PublicKeyCredentialRpEntity passkeyRelyingParty,
            @Value("${reciplease.webauthn.allowed-origins}") final String[] allowedOrigins) {
        return new Webauthn4JRelyingPartyOperations(
                userEntities, userCredentials, passkeyRelyingParty, Set.copyOf(Arrays.asList(allowedOrigins)));
    }

    // Spring Boot 4's @RestControllers serialize with Jackson 3 (tools.jackson) by default, not
    // the classic com.fasterxml.jackson WebauthnJackson2Module — this is the module that actually
    // matches the ObjectMapper Spring wires up for HTTP message conversion here.
    @Bean
    public WebauthnJacksonModule webauthnJacksonModule() {
        return new WebauthnJacksonModule();
    }
}
