package org.reciplease.dto;

import org.springframework.security.web.webauthn.api.AuthenticatorAttestationResponse;
import org.springframework.security.web.webauthn.api.PublicKeyCredential;

/**
 * Body of the passkey register/signup finish endpoints. {@code challenge} is the value the
 * matching {@code .../options} call issued — {@link org.reciplease.controller.PasskeyController}
 * looks it up in the challenge ledger and rebuilds the rest of the original creation options
 * itself, rather than trusting (or even being able to deserialize — Spring Security's WebAuthn
 * Jackson module has no deserializer for {@code PublicKeyCredentialCreationOptions}) anything
 * else the caller might echo back.
 */
public record PasskeyRegisterFinishRequest(
        String challenge, PublicKeyCredential<AuthenticatorAttestationResponse> credential, String label) {}
