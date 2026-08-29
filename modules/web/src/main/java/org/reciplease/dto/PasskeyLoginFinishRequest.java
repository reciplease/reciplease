package org.reciplease.dto;

import org.springframework.security.web.webauthn.api.AuthenticatorAssertionResponse;
import org.springframework.security.web.webauthn.api.PublicKeyCredential;

/**
 * Body of {@code POST /api/passkey/login/finish}. See {@link PasskeyRegisterFinishRequest}
 * for why this carries just the challenge rather than the full original request options.
 */
public record PasskeyLoginFinishRequest(
        String challenge, PublicKeyCredential<AuthenticatorAssertionResponse> credential) {}
