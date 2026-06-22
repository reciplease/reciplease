package org.reciplease.controller;

import lombok.RequiredArgsConstructor;
import org.reciplease.dto.IdentitiesDto;
import org.reciplease.dto.MeDto;
import org.reciplease.dto.SetHandleRequest;
import org.reciplease.repository.HandleTakenException;
import org.reciplease.repository.UserIdentityRepository;
import org.reciplease.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The current authenticated user's own profile: their id/handle ({@code /api/me}), the
 * provider names (never provider ids) of their linked identities ({@code /api/me/identities}),
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
        return userRepository.findById(currentUserId())
                .map(user -> ResponseEntity.ok(new MeDto(user.id(), user.handle())))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("identities")
    @PreAuthorize("isAuthenticated()")
    public IdentitiesDto identities() {
        return new IdentitiesDto(userIdentityRepository.findProvidersForUser(currentUserId()));
    }

    @PostMapping("handle")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MeDto> setHandle(@RequestBody final SetHandleRequest request) {
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
