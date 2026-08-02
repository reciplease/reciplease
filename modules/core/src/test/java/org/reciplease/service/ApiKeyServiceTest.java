package org.reciplease.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.reciplease.model.ApiKey;
import org.reciplease.model.HouseRole;
import org.reciplease.repository.ApiKeyRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@MockitoSettings
class ApiKeyServiceTest {

    private static final String HOUSE_ID = "house-1";

    @Mock
    private ApiKeyRepository apiKeyRepository;
    @Mock
    private ApiKeyGenerator apiKeyGenerator;

    private ApiKeyService apiKeyService;

    @BeforeEach
    void setUp() {
        apiKeyService = new ApiKeyService(apiKeyRepository, apiKeyGenerator);
    }

    @Test
    void createGeneratesAndPersistsAKeyReturningTheRawSecretOnlyOnce() {
        when(apiKeyGenerator.generate()).thenReturn("rcpl_abcdefghij1234567890");
        when(apiKeyRepository.create(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var created = apiKeyService.create(HOUSE_ID, "Home Assistant", HouseRole.READ_ONLY, "owner-1");

        assertThat(created.rawKey(), is("rcpl_abcdefghij1234567890"));
        assertThat(created.apiKey().houseId(), is(HOUSE_ID));
        assertThat(created.apiKey().name(), is("Home Assistant"));
        assertThat(created.apiKey().role(), is(HouseRole.READ_ONLY));
        assertThat(created.apiKey().createdByUserId(), is("owner-1"));
        assertThat(created.apiKey().keyPrefix(), is("rcpl_abcdefghij"));
        assertThat(created.apiKey().keyHash(), is(not(created.rawKey())));
        assertThat(created.apiKey().keyHash(), is(ApiKeyHasher.hash(created.rawKey())));
    }

    @Test
    void listReturnsAllKeysForTheHouse() {
        var key = apiKey("key-1");
        when(apiKeyRepository.findAllForHouse(HOUSE_ID)).thenReturn(List.of(key));

        assertThat(apiKeyService.list(HOUSE_ID), contains(key));
    }

    @Test
    void revokeDeletesAKeyBelongingToTheHouse() {
        when(apiKeyRepository.findById("key-1")).thenReturn(Optional.of(apiKey("key-1")));

        var revoked = apiKeyService.revoke(HOUSE_ID, "key-1");

        assertThat(revoked, is(true));
        verify(apiKeyRepository).delete("key-1");
    }

    @Test
    void revokeRefusesToDeleteAnotherHousesKey() {
        var key = new ApiKey("key-1", "other-house", "name", HouseRole.READ_ONLY, "owner-1", "prefix", "hash", Instant.now(), null);
        when(apiKeyRepository.findById("key-1")).thenReturn(Optional.of(key));

        var revoked = apiKeyService.revoke(HOUSE_ID, "key-1");

        assertThat(revoked, is(false));
        verify(apiKeyRepository, never()).delete(any());
    }

    @Test
    void revokeReturnsFalseWhenTheKeyDoesNotExist() {
        when(apiKeyRepository.findById("missing")).thenReturn(Optional.empty());

        var revoked = apiKeyService.revoke(HOUSE_ID, "missing");

        assertThat(revoked, is(false));
        verify(apiKeyRepository, never()).delete(any());
    }

    @Test
    void authenticateReturnsThePrincipalForAMatchingKeyAndRecordsLastUsedAt() {
        var rawKey = "rcpl_abcdefghij1234567890";
        var hash = ApiKeyHasher.hash(rawKey);
        var key = new ApiKey("key-1", HOUSE_ID, "name", HouseRole.OWNER, "owner-1", "rcpl_abcdefghij", hash, Instant.now(), null);
        when(apiKeyRepository.findByPrefix("rcpl_abcdefghij")).thenReturn(Optional.of(key));

        var authenticated = apiKeyService.authenticate(rawKey);

        assertThat(authenticated.isPresent(), is(true));
        assertThat(authenticated.get().apiKeyId(), is("key-1"));
        assertThat(authenticated.get().houseId(), is(HOUSE_ID));
        assertThat(authenticated.get().role(), is(HouseRole.OWNER));
        verify(apiKeyRepository).updateLastUsedAt(eq("key-1"), any());
    }

    @Test
    void authenticateReturnsEmptyWhenTheHashDoesNotMatch() {
        var key = new ApiKey("key-1", HOUSE_ID, "name", HouseRole.OWNER, "owner-1", "rcpl_abcdefghij",
                ApiKeyHasher.hash("rcpl_someotherkey1234567"), Instant.now(), null);
        when(apiKeyRepository.findByPrefix("rcpl_abcdefghij")).thenReturn(Optional.of(key));

        var authenticated = apiKeyService.authenticate("rcpl_abcdefghij1234567890");

        assertThat(authenticated, is(Optional.empty()));
        verify(apiKeyRepository, never()).updateLastUsedAt(any(), any());
    }

    @Test
    void authenticateReturnsEmptyWhenNoKeyHasThatPrefix() {
        when(apiKeyRepository.findByPrefix("rcpl_abcdefghij")).thenReturn(Optional.empty());

        var authenticated = apiKeyService.authenticate("rcpl_abcdefghij1234567890");

        assertThat(authenticated, is(Optional.empty()));
    }

    @Test
    void authenticateReturnsEmptyForATokenThatIsTooShortToContainAPrefix() {
        var authenticated = apiKeyService.authenticate("rcpl_short");

        assertThat(authenticated, is(Optional.empty()));
        verify(apiKeyRepository, never()).findByPrefix(any());
    }

    private static ApiKey apiKey(final String id) {
        return new ApiKey(id, HOUSE_ID, "name", HouseRole.READ_ONLY, "owner-1", "prefix", "hash", Instant.now(), null);
    }
}
