package org.reciplease.controller;

import lombok.RequiredArgsConstructor;
import org.reciplease.dto.GoogleHealthConnectionStatusDto;
import org.reciplease.dto.GoogleHealthTokensRequest;
import org.reciplease.dto.LogGoogleHealthFoodRequest;
import org.reciplease.model.GoogleHealthConnection;
import org.reciplease.service.GoogleHealthNotConnectedException;
import org.reciplease.service.GoogleHealthService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

/**
 * Links/unlinks the current user's Google Health account and proxies Google Health's food
 * search/logging endpoints on their behalf. The Google OAuth2 authorize URL, the
 * authorization-code exchange, and all token refreshes are handled entirely by the Next.js
 * frontend (which already holds the Google OAuth client id/secret for NextAuth sign-in) — this
 * controller never talks to Google's OAuth endpoints and never holds the client secret. The
 * frontend pushes the resulting tokens here via {@code PUT /connection} for storage, both for
 * the initial link and for every subsequent refresh.
 */
@RestController
@RequestMapping("api/google-health")
@RequiredArgsConstructor
public class GoogleHealthController {

    private final GoogleHealthService googleHealthService;

    @GetMapping("connection")
    @PreAuthorize("isAuthenticated()")
    public GoogleHealthConnectionStatusDto connection() {
        return toDto(googleHealthService.connectionStatus(currentUserId()));
    }

    @PutMapping("connection")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<GoogleHealthConnectionStatusDto> putConnection(@RequestBody final GoogleHealthTokensRequest request) {
        final var connection = googleHealthService.storeConnection(currentUserId(), request.accessToken(), request.refreshToken(),
                request.expiresIn(), request.scope());
        return ResponseEntity.ok(toDto(Optional.of(connection)));
    }

    @DeleteMapping("connection")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> disconnect() {
        googleHealthService.disconnect(currentUserId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("foods/search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> searchFoods(@RequestParam final String query) {
        try {
            final var results = googleHealthService.searchFoods(currentUserId(), query);
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(results);
        } catch (final GoogleHealthNotConnectedException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("foods/log")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> logFood(@RequestBody final LogGoogleHealthFoodRequest request) {
        try {
            googleHealthService.logFood(currentUserId(), request.foodId(), request.foodDisplayName(), request.mealType(),
                    request.amount(), request.date());
            return ResponseEntity.ok().build();
        } catch (final GoogleHealthNotConnectedException e) {
            return ResponseEntity.notFound().build();
        }
    }

    private static GoogleHealthConnectionStatusDto toDto(final Optional<GoogleHealthConnection> connection) {
        return connection
                .map(c -> new GoogleHealthConnectionStatusDto(true, c.expiresAt(), c.refreshToken()))
                .orElseGet(GoogleHealthConnectionStatusDto::disconnected);
    }

    private String currentUserId() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
