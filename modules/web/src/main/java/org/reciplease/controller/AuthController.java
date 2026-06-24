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

        try {
            final User user = request.linkToken() != null
                    ? link(request.linkToken(), provider, providerId)
                    : loginOrSignup(provider, providerId);

            final var token = jwtService.mint(user.id());
            return ResponseEntity.ok(new ExchangeResponse(token, user.id(), user.handle()));
        } catch (final IdentityConflictException e) {
            return ResponseEntity.status(409).build();
        }
    }

    private User link(final String linkToken, final String provider, final String providerId) {
        final var userId = jwtService.parse(linkToken)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired linkToken"));
        userRepository.linkIdentity(userId, provider, providerId);
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("Missing user referenced by a valid linkToken: " + userId));
    }

    private User loginOrSignup(final String provider, final String providerId) {
        return userRepository.findByIdentity(provider, providerId)
                .orElseGet(() -> userRepository.createWithIdentity(provider, providerId));
    }

    private boolean isValidSecret(final String providedSecret) {
        return providedSecret != null
                && MessageDigest.isEqual(providedSecret.getBytes(StandardCharsets.UTF_8), internalSecret.getBytes(StandardCharsets.UTF_8));
    }
}
