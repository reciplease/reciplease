package org.reciplease.configuration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.reciplease.model.ApiKeyPrincipal;
import org.reciplease.model.HouseRole;
import org.reciplease.repository.HouseRepository;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@MockitoSettings
class HouseAccessTest {

    @Mock
    private HouseRepository houseRepository;

    private HouseAccess houseAccess;

    @BeforeEach
    void setUp() {
        houseAccess = new HouseAccess(houseRepository);
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void isMemberIsTrueForAnApiKeyWhoseHouseMatchesTheRequestHeader() {
        withHouseHeader("house-1");
        SecurityContextHolder.getContext().setAuthentication(
                new ApiKeyAuthenticationToken(new ApiKeyPrincipal("key-1", "house-1", HouseRole.READ_ONLY)));

        assertThat(houseAccess.isMember(), is(true));
        assertThat(houseAccess.isOwner(), is(false));
    }

    @Test
    void isOwnerIsTrueForAnOwnerScopedApiKey() {
        withHouseHeader("house-1");
        SecurityContextHolder.getContext().setAuthentication(
                new ApiKeyAuthenticationToken(new ApiKeyPrincipal("key-1", "house-1", HouseRole.OWNER)));

        assertThat(houseAccess.isOwner(), is(true));
    }

    @Test
    void isMemberIsFalseWhenTheApiKeysHouseDoesNotMatchTheRequestHeader() {
        withHouseHeader("house-2");
        SecurityContextHolder.getContext().setAuthentication(
                new ApiKeyAuthenticationToken(new ApiKeyPrincipal("key-1", "house-1", HouseRole.OWNER)));

        assertThat(houseAccess.isMember(), is(false));
    }

    @Test
    void isMemberIsFalseForAnApiKeyWhenTheRequestHasNoHouseHeaderAtAll() {
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));
        SecurityContextHolder.getContext().setAuthentication(
                new ApiKeyAuthenticationToken(new ApiKeyPrincipal("key-1", "house-1", HouseRole.OWNER)));

        assertThat(houseAccess.isMember(), is(false));
    }

    private static void withHouseHeader(final String houseId) {
        final var request = new MockHttpServletRequest();
        request.addHeader(HouseAccess.HOUSE_HEADER, houseId);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }
}
