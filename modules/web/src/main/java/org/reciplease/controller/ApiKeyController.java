package org.reciplease.controller;

import static java.util.stream.Collectors.toList;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.reciplease.configuration.ApiKeyAuthenticationToken;
import org.reciplease.configuration.CurrentHouse;
import org.reciplease.configuration.HouseAccess;
import org.reciplease.configuration.HouseOwner;
import org.reciplease.dto.ApiKeyDto;
import org.reciplease.dto.ApiKeySelfDto;
import org.reciplease.dto.CreateApiKeyRequest;
import org.reciplease.dto.CreatedApiKeyDto;
import org.reciplease.repository.HouseRepository;
import org.reciplease.service.ApiKeyService;
import org.springframework.http.HttpStatus;
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
 * Manages house service-account API keys — long-lived credentials a house owner mints for a
 * third-party client that can't do an interactive sign-in (e.g. a Home Assistant integration).
 * Owner-only: a service account acts with a role the owner picks, so minting one is itself a
 * grant of access, same as creating an invite.
 */
@RestController
@RequestMapping("api/houses/api-keys")
@Tag(name = "API Keys")
@RequiredArgsConstructor
public class ApiKeyController {

    private final ApiKeyService apiKeyService;
    private final HouseAccess houseAccess;
    private final HouseRepository houseRepository;

    @GetMapping
    @HouseOwner
    @Operation(operationId = "findAllApiKeys")
    public ResponseEntity<List<ApiKeyDto>> findAll(@CurrentHouse final String houseId) {
        final var keys =
                apiKeyService.list(houseId).stream().map(ApiKeyDto::from).collect(toList());
        return ResponseEntity.ok(keys);
    }

    @PostMapping
    @HouseOwner
    @Operation(operationId = "createApiKey")
    public ResponseEntity<CreatedApiKeyDto> create(
            @CurrentHouse final String houseId, @Valid @RequestBody final CreateApiKeyRequest request) {
        final var created = apiKeyService.create(houseId, request.getName(), request.getRole(), currentUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(CreatedApiKeyDto.from(created));
    }

    @DeleteMapping("{id}")
    @HouseOwner
    @Operation(operationId = "revokeApiKey")
    public ResponseEntity<Void> revoke(@CurrentHouse final String houseId, @PathVariable final String id) {
        final var revoked = apiKeyService.revoke(houseId, id);
        return revoked
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    /**
     * Lets an API-key-authenticated caller discover the house/role its own key resolves to,
     * since the raw key is opaque and carries nothing a client can read directly — a service
     * account needs this before it knows what to send as {@code X-RCPLS-House-Id} on any other
     * house-scoped call. Not meaningful for a normal user JWT (a user isn't scoped to a single
     * house), so it 403s outside an API-key authentication.
     */
    @GetMapping("self")
    @PreAuthorize("hasRole('RECIPLEASE')")
    @Operation(operationId = "findApiKeySelf")
    public ResponseEntity<ApiKeySelfDto> self() {
        final var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof ApiKeyAuthenticationToken apiKeyAuthentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        final var principal = apiKeyAuthentication.getPrincipal();
        final var houseName = houseRepository
                .findById(principal.houseId())
                .map(house -> house.name())
                .orElse(null);
        return ResponseEntity.ok(ApiKeySelfDto.builder()
                .houseId(principal.houseId())
                .houseName(houseName)
                .role(principal.role())
                .build());
    }

    private String currentUserId() {
        final var authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated() ? authentication.getName() : null;
    }
}
