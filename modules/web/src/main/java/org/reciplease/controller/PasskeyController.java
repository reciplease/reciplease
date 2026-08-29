package org.reciplease.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.reciplease.configuration.ReciplaseJwtService;
import org.reciplease.dto.ExchangeResponse;
import org.reciplease.dto.PasskeyLoginFinishRequest;
import org.reciplease.dto.PasskeyRegisterFinishRequest;
import org.reciplease.model.User;
import org.reciplease.repository.IdentityConflictException;
import org.reciplease.repository.UserRepository;
import org.reciplease.repository.WebAuthnChallengeLedger;
import org.reciplease.service.RefreshTokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.webauthn.api.AuthenticatorSelectionCriteria;
import org.springframework.security.web.webauthn.api.Bytes;
import org.springframework.security.web.webauthn.api.ImmutablePublicKeyCredentialUserEntity;
import org.springframework.security.web.webauthn.api.PublicKeyCredentialCreationOptions;
import org.springframework.security.web.webauthn.api.PublicKeyCredentialParameters;
import org.springframework.security.web.webauthn.api.PublicKeyCredentialRequestOptions;
import org.springframework.security.web.webauthn.api.PublicKeyCredentialRpEntity;
import org.springframework.security.web.webauthn.api.PublicKeyCredentialUserEntity;
import org.springframework.security.web.webauthn.api.ResidentKeyRequirement;
import org.springframework.security.web.webauthn.api.UserVerificationRequirement;
import org.springframework.security.web.webauthn.management.ImmutablePublicKeyCredentialCreationOptionsRequest;
import org.springframework.security.web.webauthn.management.ImmutablePublicKeyCredentialRequestOptionsRequest;
import org.springframework.security.web.webauthn.management.ImmutableRelyingPartyRegistrationRequest;
import org.springframework.security.web.webauthn.management.RelyingPartyAuthenticationRequest;
import org.springframework.security.web.webauthn.management.RelyingPartyPublicKey;
import org.springframework.security.web.webauthn.management.WebAuthnRelyingPartyOperations;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Passkeys as a third sign-in provider, alongside Google/GitHub — but unlike those, there's no
 * external identity to verify out of band, so the WebAuthn ceremony has to happen here directly.
 * <p>
 * This bypasses Spring Security's {@code .webAuthn()} DSL (and its default
 * {@code /webauthn/register}/{@code /login/webauthn} endpoints) entirely: that DSL assumes
 * session-based, CSRF-protected form login and stashes the in-flight challenge in the
 * {@code HttpSession}, neither of which exist in this app's stateless bearer-JWT setup. Instead,
 * {@link WebAuthnRelyingPartyOperations} is called directly.
 * <p>
 * The finish endpoints also can't simply have the client echo back the original creation/request
 * options the way the default flow would stash and retrieve from the session: Spring Security's
 * WebAuthn Jackson module only ever serializes those types, it has no deserializer for them. So
 * {@link WebAuthnChallengeLedger} (a tiny, TTL-expiring Mongo collection — the stand-in for the
 * session) remembers just the challenge and the user id it was issued for, and the finish methods
 * rebuild an equivalent options object themselves from that, rather than trusting (or being able
 * to parse) anything the client sends beyond the challenge value and its own credential response.
 * <p>
 * Three flows:
 * <ul>
 * <li><b>signup</b> (anonymous) — registers a credential for a brand-new Reciplease user, so a
 * passkey can be someone's only sign-in method, not just a convenience added to an existing one.
 * <li><b>register</b> (authenticated) — links a passkey to the already signed-in user, exactly
 * like linking a second OAuth provider.
 * <li><b>login</b> (anonymous) — authenticates an existing passkey. Usernameless/discoverable:
 * the browser's own passkey picker resolves which credential, not a server-supplied allow-list.
 * </ul>
 */
@RestController
@RequestMapping("api/passkey")
@Tag(name = "Passkeys")
@RequiredArgsConstructor
public class PasskeyController {

    private static final Logger log = LoggerFactory.getLogger(PasskeyController.class);

    // EdDSA, ES256, RS256 — the same algorithm set Webauthn4JRelyingPartyOperations itself
    // generates by default, since these reconstructed options must match what the browser
    // was actually shown when the credential was created.
    private static final List<PublicKeyCredentialParameters> PUB_KEY_CRED_PARAMS = List.of(
            PublicKeyCredentialParameters.EdDSA,
            PublicKeyCredentialParameters.ES256,
            PublicKeyCredentialParameters.RS256);

    private final WebAuthnRelyingPartyOperations relyingPartyOperations;
    private final WebAuthnChallengeLedger challengeLedger;
    private final PublicKeyCredentialRpEntity passkeyRelyingParty;
    private final UserRepository userRepository;
    private final ReciplaseJwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("signup/options")
    @Operation(operationId = "passkeySignupOptions")
    public PublicKeyCredentialCreationOptions signupOptions() {
        // The eventual user id is minted now, not at signup/finish — WebAuthn embeds the user
        // handle into the credential at creation time, so it has to be decided up front.
        final var pendingUserId = UUID.randomUUID().toString();
        return creationOptions(new UsernamePasswordAuthenticationToken(pendingUserId, null, List.of()), pendingUserId);
    }

    @PostMapping("signup/finish")
    @Operation(operationId = "passkeySignupFinish")
    public ResponseEntity<ExchangeResponse> signupFinish(@RequestBody final PasskeyRegisterFinishRequest request) {
        final var userId =
                challengeLedger.consumeForRegistration(request.challenge()).orElse(null);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        final var credential = registerCredential(userId, request);
        userRepository.save(new User(userId, null));
        try {
            userRepository.linkIdentity(userId, "passkey", credential, null);
        } catch (final IdentityConflictException e) {
            return ResponseEntity.status(409).build();
        }

        final var issued = refreshTokenService.issue(userId);
        return ResponseEntity.ok(
                new ExchangeResponse(jwtService.mint(userId), issued.rawToken(), issued.expiresAt(), userId, null));
    }

    @PostMapping("register/options")
    @PreAuthorize("isAuthenticated()")
    @Operation(operationId = "passkeyRegisterOptions")
    public PublicKeyCredentialCreationOptions registerOptions() {
        final var authentication = SecurityContextHolder.getContext().getAuthentication();
        return creationOptions(authentication, authentication.getName());
    }

    @PostMapping("register/finish")
    @PreAuthorize("isAuthenticated()")
    @Operation(operationId = "passkeyRegisterFinish")
    public ResponseEntity<Void> registerFinish(@RequestBody final PasskeyRegisterFinishRequest request) {
        final var userId =
                SecurityContextHolder.getContext().getAuthentication().getName();
        if (challengeLedger
                .consumeForRegistration(request.challenge())
                .filter(userId::equals)
                .isEmpty()) {
            return ResponseEntity.status(401).build();
        }

        final var credential = registerCredential(userId, request);
        try {
            userRepository.linkIdentity(userId, "passkey", credential, null);
        } catch (final IdentityConflictException e) {
            return ResponseEntity.status(409).build();
        }

        return ResponseEntity.ok().build();
    }

    @PostMapping("login/options")
    @Operation(operationId = "passkeyLoginOptions")
    public PublicKeyCredentialRequestOptions loginOptions() {
        // An anonymous token resolves to an empty allow-list (see Webauthn4JRelyingPartyOperations),
        // which is exactly the discoverable/usernameless login this app wants.
        final var anonymous = new AnonymousAuthenticationToken(
                "passkey-login", "anonymousUser", List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS")));
        final var options = relyingPartyOperations.createCredentialRequestOptions(
                new ImmutablePublicKeyCredentialRequestOptionsRequest(anonymous));
        challengeLedger.issue(options.getChallenge().toBase64UrlString(), null);
        return options;
    }

    @PostMapping("login/finish")
    @Operation(operationId = "passkeyLoginFinish")
    public ResponseEntity<ExchangeResponse> loginFinish(@RequestBody final PasskeyLoginFinishRequest request) {
        if (!challengeLedger.consumeForLogin(request.challenge())) {
            return ResponseEntity.status(401).build();
        }

        final var requestOptions = PublicKeyCredentialRequestOptions.builder()
                .challenge(Bytes.fromBase64(request.challenge()))
                .rpId(passkeyRelyingParty.getId())
                .build();

        final PublicKeyCredentialUserEntity userEntity;
        try {
            userEntity = relyingPartyOperations.authenticate(
                    new RelyingPartyAuthenticationRequest(requestOptions, request.credential()));
        } catch (final RuntimeException e) {
            log.warn("Passkey login/finish authentication failed", e);
            return ResponseEntity.status(401).build();
        }

        final var userId = toUserId(userEntity.getId());
        final var handle = userRepository.findById(userId).map(User::handle).orElse(null);
        final var issued = refreshTokenService.issue(userId);
        return ResponseEntity.ok(
                new ExchangeResponse(jwtService.mint(userId), issued.rawToken(), issued.expiresAt(), userId, handle));
    }

    // These values match what Webauthn4JRelyingPartyOperations.createPublicKeyCredentialCreationOptions
    // always emits — verified from bytecode — so the rebuilt options are equivalent to what the
    // browser received during the options phase.
    private static final AuthenticatorSelectionCriteria PASSKEY_AUTHENTICATOR_SELECTION =
            AuthenticatorSelectionCriteria.builder()
                    .userVerification(UserVerificationRequirement.PREFERRED)
                    .residentKey(ResidentKeyRequirement.REQUIRED)
                    .build();

    /** Registers {@code request}'s credential against a freshly rebuilt options object, returning the new credential's id. */
    private String registerCredential(final String userId, final PasskeyRegisterFinishRequest request) {
        final var creationOptions = PublicKeyCredentialCreationOptions.builder()
                .rp(passkeyRelyingParty)
                .user(userEntityFor(userId))
                .challenge(Bytes.fromBase64(request.challenge()))
                .pubKeyCredParams(PUB_KEY_CRED_PARAMS)
                .authenticatorSelection(PASSKEY_AUTHENTICATOR_SELECTION)
                .build();
        final var label = request.label() != null ? request.label() : "";
        final var credential = relyingPartyOperations.registerCredential(new ImmutableRelyingPartyRegistrationRequest(
                creationOptions, new RelyingPartyPublicKey(request.credential(), label)));
        return credential.getCredentialId().toBase64UrlString();
    }

    private PublicKeyCredentialCreationOptions creationOptions(
            final Authentication authentication, final String userId) {
        // Built via the library (not the manual builder registerCredential uses) so the response
        // includes everything the browser needs to pick an authenticator — excludeCredentials,
        // authenticatorSelection, attestation, extensions — none of which matter for verification
        // (see registerCredential) but do matter for a sensible ceremony.
        final var options = relyingPartyOperations.createPublicKeyCredentialCreationOptions(
                new ImmutablePublicKeyCredentialCreationOptionsRequest(authentication));
        challengeLedger.issue(options.getChallenge().toBase64UrlString(), userId);
        return options;
    }

    private static PublicKeyCredentialUserEntity userEntityFor(final String userId) {
        return ImmutablePublicKeyCredentialUserEntity.builder()
                .id(new Bytes(userId.getBytes(StandardCharsets.UTF_8)))
                .name(userId)
                .displayName(userId)
                .build();
    }

    // WebAuthn's "user handle" is just the Reciplease user id's UTF-8 bytes (see the matching
    // encode in the database module's PasskeyUserEntityRepositoryImpl) — deterministic in both
    // directions, so no separate user-entity storage/lookup is needed on either side.
    private static String toUserId(final Bytes handle) {
        return new String(handle.getBytes(), StandardCharsets.UTF_8);
    }
}
