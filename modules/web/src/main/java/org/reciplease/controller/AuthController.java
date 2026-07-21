package org.reciplease.controller;

import lombok.RequiredArgsConstructor;
import org.reciplease.configuration.ReciplaseJwtService;
import org.reciplease.dto.ExchangeRequest;
import org.reciplease.dto.ExchangeResponse;
import org.reciplease.model.User;
import org.reciplease.repository.IdentityConflictException;
import org.reciplease.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Exchanges a provider sign-in (already verified by the frontend) for a Reciplease JWT.
 * This is how a caller obtains a Reciplease JWT in the first place, so it is excluded from
 * the normal bearer-JWT auth filter chain (see {@code CloudWebSecurityConfig}) and instead
 * authenticates the caller via the {@code X-Internal-Secret} header, which only the frontend's
 * server-side code should know.
 */
@RestController
@RequestMapping("api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final ReciplaseJwtService jwtService;

    @Value("${reciplease.jwt.signing-secret}")
    private String internalSecret;

    @PostMapping("exchange")
    public ResponseEntity<ExchangeResponse> exchange(
            @RequestHeader(value = "X-Internal-Secret", required = false) final String providedSecret,
            @RequestBody final ExchangeRequest request) {
        if (!isValidSecret(providedSecret)) {
            return ResponseEntity.status(401).build();
        }

        final String provider = request.provider();
        final String providerId = request.providerId();
        final String email = request.email();

        try {
            final User user = request.linkToken() != null
                    ? link(request.linkToken(), provider, providerId, email)
                    : loginOrSignup(provider, providerId, email);

            final var token = jwtService.mint(user.id());
            return ResponseEntity.ok(new ExchangeResponse(token, user.id(), user.handle()));
        } catch (final IdentityConflictException e) {
            return ResponseEntity.status(409).build();
        }
    }

    private User link(final String linkToken, final String provider, final String providerId, final String email) {
        final var userId = jwtService.parse(linkToken)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired linkToken"));
        userRepository.linkIdentity(userId, provider, providerId, email);
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("Missing user referenced by a valid linkToken: " + userId));
    }

    private User loginOrSignup(final String provider, final String providerId, final String email) {
        final var existingUser = userRepository.findByIdentity(provider, providerId);
        if (existingUser.isPresent()) {
            // Keep the stored email current — it's only ever set at link time otherwise, so it'd
            // go stale (or stay null for identities linked before we captured email at all).
            if (email != null) {
                userRepository.updateIdentityEmail(provider, providerId, email);
            }
            return existingUser.get();
        }
        return userRepository.createWithIdentity(provider, providerId, email);
    }

    /**
     * Mints a fresh Reciplease JWT for the already-authenticated caller — a sliding session:
     * called by the frontend shortly before the current token expires so an active user is
     * silently kept signed in, without a full provider re-auth. Runs through the normal
     * bearer-JWT filter chain (unlike {@link #exchange}), so it requires a currently-valid
     * token to begin with; there is no separate refresh token to fall back on.
     */
    @PostMapping("refresh")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ExchangeResponse> refresh() {
        final String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        final String handle = userRepository.findById(userId).map(User::handle).orElse(null);
        return ResponseEntity.ok(new ExchangeResponse(jwtService.mint(userId), userId, handle));
    }

    private boolean isValidSecret(final String providedSecret) {
        return providedSecret != null
                && MessageDigest.isEqual(providedSecret.getBytes(StandardCharsets.UTF_8), internalSecret.getBytes(StandardCharsets.UTF_8));
    }
}
