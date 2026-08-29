package org.reciplease.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.reciplease.model.ApiKey;
import org.reciplease.model.ApiKeyPrincipal;
import org.reciplease.model.CreatedApiKey;
import org.reciplease.model.HouseRole;
import org.reciplease.repository.ApiKeyRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ApiKeyService {

    private static final int PREFIX_LENGTH = 15;

    private final ApiKeyRepository apiKeyRepository;
    private final ApiKeyGenerator apiKeyGenerator;

    /**
     * Mints and persists a new house-scoped service-account key. The raw secret is only ever
     * available on the returned {@link CreatedApiKey} — only its hash is stored.
     */
    public CreatedApiKey create(
            final String houseId, final String name, final HouseRole role, final String createdByUserId) {
        final var rawKey = apiKeyGenerator.generate();
        final var apiKey = new ApiKey(
                null,
                houseId,
                name,
                role,
                createdByUserId,
                prefixOf(rawKey),
                ApiKeyHasher.hash(rawKey),
                Instant.now(),
                null);
        return new CreatedApiKey(apiKeyRepository.create(apiKey), rawKey);
    }

    public List<ApiKey> list(final String houseId) {
        return apiKeyRepository.findAllForHouse(houseId);
    }

    /**
     * Deletes API key {@code id}, but only if it belongs to {@code houseId} — mirrors the
     * house-scoped delete checks elsewhere so an owner of one house can't revoke another
     * house's key by guessing its id. Returns false if the key doesn't exist or belongs to a
     * different house.
     */
    public boolean revoke(final String houseId, final String id) {
        final var belongsToHouse = apiKeyRepository
                .findById(id)
                .filter(key -> key.houseId().equals(houseId))
                .isPresent();
        if (belongsToHouse) {
            apiKeyRepository.delete(id);
        }
        return belongsToHouse;
    }

    /**
     * Resolves a raw bearer token to the house/role it acts as, in constant time against the
     * stored hash. Empty if the token is malformed, unknown, or its hash doesn't match.
     */
    public Optional<ApiKeyPrincipal> authenticate(final String rawKey) {
        if (rawKey.length() < PREFIX_LENGTH) {
            return Optional.empty();
        }
        final var candidateHash = ApiKeyHasher.hash(rawKey);
        return apiKeyRepository
                .findByPrefix(prefixOf(rawKey))
                .filter(key -> constantTimeEquals(key.keyHash(), candidateHash))
                .map(key -> {
                    apiKeyRepository.updateLastUsedAt(key.id(), Instant.now());
                    return new ApiKeyPrincipal(key.id(), key.houseId(), key.role());
                });
    }

    private static String prefixOf(final String rawKey) {
        return rawKey.substring(0, PREFIX_LENGTH);
    }

    private static boolean constantTimeEquals(final String a, final String b) {
        return MessageDigest.isEqual(a.getBytes(StandardCharsets.UTF_8), b.getBytes(StandardCharsets.UTF_8));
    }
}
