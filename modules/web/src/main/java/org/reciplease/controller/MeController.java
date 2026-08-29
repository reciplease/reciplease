package org.reciplease.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.reciplease.dto.IdentitiesDto;
import org.reciplease.dto.LinkedIdentityDto;
import org.reciplease.dto.MeDto;
import org.reciplease.dto.SetHandleRequest;
import org.reciplease.model.LinkedIdentity;
import org.reciplease.repository.HandleTakenException;
import org.reciplease.repository.UserIdentityRepository;
import org.reciplease.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The current authenticated user's own profile: their id/handle ({@code /api/me}), their
 * linked identities (provider + email, never the provider id, {@code /api/me/identities}),
 * and setting their handle ({@code /api/me/handle}).
 */
@RestController
@RequestMapping("api/me")
@RequiredArgsConstructor
public class MeController {

    private final UserRepository userRepository;
    private final UserIdentityRepository userIdentityRepository;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MeDto> me() {
        return userRepository
                .findById(currentUserId())
                .map(user -> ResponseEntity.ok(new MeDto(user.id(), user.handle())))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("identities")
    @PreAuthorize("isAuthenticated()")
    public IdentitiesDto identities() {
        return toDto(userIdentityRepository.findIdentitiesForUser(currentUserId()));
    }

    @DeleteMapping("identities/{provider}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<IdentitiesDto> unlinkIdentity(@PathVariable final String provider) {
        final var userId = currentUserId();
        final var identities = userIdentityRepository.findIdentitiesForUser(userId);
        if (identities.stream().noneMatch(identity -> identity.provider().equals(provider))) {
            return ResponseEntity.notFound().build();
        }
        // Refuse to remove the only sign-in method — it would lock the user out.
        if (identities.size() <= 1) {
            return ResponseEntity.status(409).build();
        }
        userIdentityRepository.removeForUser(userId, provider);
        return ResponseEntity.ok(toDto(userIdentityRepository.findIdentitiesForUser(userId)));
    }

    private static IdentitiesDto toDto(final List<LinkedIdentity> identities) {
        return new IdentitiesDto(identities.stream()
                .map(identity -> new LinkedIdentityDto(identity.provider(), identity.email()))
                .toList());
    }

    @PostMapping("handle")
    @PreAuthorize("isAuthenticated()")
    @Operation(operationId = "setHandle")
    public ResponseEntity<MeDto> setHandle(@Valid @RequestBody final SetHandleRequest request) {
        final var userId = currentUserId();
        final var handle = request.handle() == null ? "" : request.handle().trim();
        final var codePoints = handle.codePointCount(0, handle.length());
        if (codePoints < 1 || codePoints > 30) {
            return ResponseEntity.badRequest().build();
        }

        try {
            userRepository.setHandle(userId, handle);
        } catch (final HandleTakenException e) {
            return ResponseEntity.status(409).build();
        }
        return ResponseEntity.ok(new MeDto(userId, handle));
    }

    private String currentUserId() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
