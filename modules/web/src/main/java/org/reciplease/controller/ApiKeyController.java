package org.reciplease.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.reciplease.configuration.HouseAccess;
import org.reciplease.configuration.HouseOwner;
import org.reciplease.dto.ApiKeyDto;
import org.reciplease.dto.CreateApiKeyRequest;
import org.reciplease.dto.CreatedApiKeyDto;
import org.reciplease.service.ApiKeyService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * Manages house service-account API keys — long-lived credentials a house owner mints for a
 * third-party client that can't do an interactive sign-in (e.g. a Home Assistant integration).
 * Owner-only: a service account acts with a role the owner picks, so minting one is itself a
 * grant of access, same as creating an invite.
 */
@RestController
@RequestMapping("api/houses/api-keys")
@RequiredArgsConstructor
@HouseOwner
public class ApiKeyController {

    private final ApiKeyService apiKeyService;
    private final HouseAccess houseAccess;

    @GetMapping
    public ResponseEntity<List<ApiKeyDto>> findAll() {
        final var keys = apiKeyService.list(houseAccess.requireHouseId()).stream()
                .map(ApiKeyDto::from)
                .collect(toList());
        return ResponseEntity.ok(keys);
    }

    @PostMapping
    public ResponseEntity<CreatedApiKeyDto> create(@Valid @RequestBody final CreateApiKeyRequest request) {
        final var created = apiKeyService.create(
                houseAccess.requireHouseId(), request.getName(), request.getRole(), currentUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(CreatedApiKeyDto.from(created));
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> revoke(@PathVariable final String id) {
        final var revoked = apiKeyService.revoke(houseAccess.requireHouseId(), id);
        return revoked ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    private String currentUserId() {
        final var authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated() ? authentication.getName() : null;
    }
}
