package org.reciplease.controller;

import lombok.RequiredArgsConstructor;
import org.reciplease.dto.HouseDto;
import org.reciplease.dto.InviteDto;
import org.reciplease.repository.HouseRepository;
import org.reciplease.repository.InviteRepository;
import org.reciplease.service.InviteService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Lets a user with a valid one-time code join a house: GET previews the invite (no
 * auth required, so the landing page can show "you're invited to X" before login);
 * POST redeems it. Redeeming only requires a valid Google JWT — the user does not
 * need to be on the allowlist yet, since accepting is exactly what allowlists them.
 */
@RestController
@RequestMapping("api/invites")
@RequiredArgsConstructor
public class InviteController {

    private final InviteRepository inviteRepository;
    private final HouseRepository houseRepository;
    private final InviteService inviteService;

    @GetMapping("{code}")
    public ResponseEntity<InviteDto> preview(@PathVariable final String code) {
        final var preview = inviteRepository.findByCode(code)
                .filter(invite -> invite.usedAt() == null)
                .flatMap(invite -> houseRepository.findById(invite.houseId()))
                .map(InviteDto::from);
        return ResponseEntity.of(preview);
    }

    @PostMapping("{code}/accept")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<HouseDto> accept(@PathVariable final String code) {
        final var userId = currentUserId();
        final var email = currentEmail();
        if (userId == null || email == null) {
            return ResponseEntity.status(401).build();
        }

        final var accepted = inviteService.accept(code, userId, email)
                .map(house -> HouseDto.from(house, null));
        return accepted.isPresent() ? ResponseEntity.ok(accepted.get()) : ResponseEntity.notFound().build();
    }

    private String currentUserId() {
        final var authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated() ? authentication.getName() : null;
    }

    private String currentEmail() {
        final var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof JwtAuthenticationToken jwtAuthentication)) {
            return null;
        }
        final Jwt jwt = jwtAuthentication.getToken();
        return jwt.getClaimAsString("email");
    }
}
