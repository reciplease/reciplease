package org.reciplease.controller;

import lombok.RequiredArgsConstructor;
import org.reciplease.configuration.ReciplaseJwtService;
import org.reciplease.dto.ExchangeRequest;
import org.reciplease.dto.ExchangeResponse;
import org.reciplease.model.User;
import org.reciplease.repository.IdentityConflictException;
import org.reciplease.repository.UserRepository;
import org.reciplease.service.RefreshTokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Exchanges a provider sign-in (already verified by the frontend) for a Reciplease JWT, and
 * handles the refresh-token lifecycle that keeps a session alive beyond that JWT's short
 * lifetime.
 * <p>
 * {@code /api/auth/exchange} is how a caller obtains a Reciplease JWT in the first place, so it
 * is excluded from the normal bearer-JWT auth filter chain (see {@code CloudWebSecurityConfig})
 * and instead authenticates the caller via the {@code X-Internal-Secret} header, which only the
 * frontend's server-side code should know. A fresh login/signup (not a link) also mints a
 * rotating, revocable refresh token (see {@link RefreshTokenService}), returned in the response
 * body for the caller to store as the {@code reciplease-refresh} cookie.
 * <p>
 * {@code /api/auth/refresh} redeems that refresh token — read from the {@code reciplease-refresh}
 * cookie, not a bearer token — for a new access token plus a rotated refresh token. It, and
 * {@code /api/auth/logout}, also run outside the normal bearer-JWT filter chain: a still-valid
 * access token is not required (and a stale/expired one must not 401 them), since the whole
 * point of a refresh token is to recover a session once the access token has expired.
 * <p>
 * {@code /api/auth/refresh-token} is the opposite case: a caller with a still-valid access token
 * but no refresh token at all (e.g. a session that predates refresh-token support) can backfill
 * one without a full re-login. Unlike {@code exchange}/{@code refresh}, it runs through the
 * normal bearer-JWT filter chain and requires the caller to already be authenticated.
 */
@RestController
@RequestMapping("api/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final UserRepository userRepository;
    private final ReciplaseJwtService jwtService;
    private final RefreshTokenService refreshTokenService;

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
        final boolean linking = request.linkToken() != null;

        try {
            final User user = linking
                    ? link(request.linkToken(), provider, providerId, email)
                    : loginOrSignup(provider, providerId, email);

            final var token = jwtService.mint(user.id());
            final var refreshToken = linking ? null : refreshTokenService.issue(user.id()).rawToken();
            return ResponseEntity.ok(new ExchangeResponse(token, refreshToken, user.id(), user.handle()));
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
     * Redeems the {@code reciplease-refresh} cookie for a new access token and a rotated
     * refresh token. Reused (already-redeemed) refresh tokens are treated as a theft signal —
     * the whole token family is revoked and the caller must sign in again.
     */
    @PostMapping("refresh")
    public ResponseEntity<ExchangeResponse> refresh(
            @CookieValue(value = "reciplease-refresh", required = false) final String refreshCookie) {
        if (refreshCookie == null) {
            return ResponseEntity.status(401).build();
        }
        final var result = refreshTokenService.rotate(refreshCookie);
        if (result instanceof RefreshTokenService.ReuseDetected reuse) {
            log.warn("Refresh token reuse detected for user {}", reuse.userId());
            return ResponseEntity.status(401).build();
        }
        if (!(result instanceof RefreshTokenService.Rotated rotated)) {
            return ResponseEntity.status(401).build();
        }
        final var handle = userRepository.findById(rotated.userId()).map(User::handle).orElse(null);
        return ResponseEntity.ok(new ExchangeResponse(
                jwtService.mint(rotated.userId()), rotated.rawToken(), rotated.userId(), handle));
    }

    /**
     * Backfills a refresh token for an already-authenticated caller who doesn't have one yet —
     * chiefly a session that predates refresh-token support existing at all, which otherwise has
     * no way to obtain one short of a full re-login once its access token expires. Runs through
     * the normal bearer-JWT filter chain (unlike {@link #exchange}/{@link #refresh}), so it
     * requires a currently-valid access token to begin with.
     */
    @PostMapping("refresh-token")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ExchangeResponse> issueRefreshToken() {
        final String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        final var issued = refreshTokenService.issue(userId);
        final var handle = userRepository.findById(userId).map(User::handle).orElse(null);
        return ResponseEntity.ok(new ExchangeResponse(jwtService.mint(userId), issued.rawToken(), userId, handle));
    }

    /** Revokes the refresh token carried by the {@code reciplease-refresh} cookie, if any. */
    @PostMapping("logout")
    public ResponseEntity<Void> logout(
            @CookieValue(value = "reciplease-refresh", required = false) final String refreshCookie) {
        if (refreshCookie != null) {
            refreshTokenService.revokeByRawToken(refreshCookie);
        }
        return ResponseEntity.ok().build();
    }

    private boolean isValidSecret(final String providedSecret) {
        return providedSecret != null
                && MessageDigest.isEqual(providedSecret.getBytes(StandardCharsets.UTF_8), internalSecret.getBytes(StandardCharsets.UTF_8));
    }
}
