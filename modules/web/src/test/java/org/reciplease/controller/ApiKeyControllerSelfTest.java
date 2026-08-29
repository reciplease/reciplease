package org.reciplease.controller;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.reciplease.configuration.ApiKeyAuthenticationToken;
import org.reciplease.configuration.HouseAccess;
import org.reciplease.model.ApiKeyPrincipal;
import org.reciplease.model.House;
import org.reciplease.model.HouseRole;
import org.reciplease.repository.HouseRepository;
import org.reciplease.service.ApiKeyService;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Exercises {@link ApiKeyController#self()} directly rather than through {@code @WebMvcTest} +
 * {@code MockMvc}: the class's {@code @WithHouseOwner} default authentication (needed for the
 * owner-only endpoints in {@link ApiKeyControllerTest}) takes precedence over a per-request
 * {@code SecurityMockMvcRequestPostProcessors.authentication()} override there, so this method
 * — the one endpoint that cares about the concrete authentication *type* rather than just a
 * granted authority — is easier to verify by calling it directly against a controlled
 * {@link SecurityContextHolder} instead.
 */
@MockitoSettings
class ApiKeyControllerSelfTest {

    @Mock
    private ApiKeyService apiKeyService;

    @Mock
    private HouseAccess houseAccess;

    @Mock
    private HouseRepository houseRepository;

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void resolvesTheHouseAndRoleAnApiKeyAuthenticatesAs() {
        when(houseRepository.findById("house-1"))
                .thenReturn(Optional.of(new House("house-1", "Test House", Instant.now())));
        SecurityContextHolder.getContext()
                .setAuthentication(
                        new ApiKeyAuthenticationToken(new ApiKeyPrincipal("key-1", "house-1", HouseRole.READ_ONLY)));

        var response = new ApiKeyController(apiKeyService, houseAccess, houseRepository).self();

        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(response.getBody().getHouseId(), is("house-1"));
        assertThat(response.getBody().getHouseName(), is("Test House"));
        assertThat(response.getBody().getRole(), is(HouseRole.READ_ONLY));
    }

    @Test
    void isForbiddenForANonApiKeyAuthentication() {
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("user-1", "n/a"));

        var response = new ApiKeyController(apiKeyService, houseAccess, houseRepository).self();

        assertThat(response.getStatusCode(), is(HttpStatus.FORBIDDEN));
    }
}
