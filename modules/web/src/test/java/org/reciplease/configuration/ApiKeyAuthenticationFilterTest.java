package org.reciplease.configuration;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.reciplease.model.ApiKeyPrincipal;
import org.reciplease.model.HouseRole;
import org.reciplease.service.ApiKeyService;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@MockitoSettings
class ApiKeyAuthenticationFilterTest {

    @Mock
    private ApiKeyService apiKeyService;
    @Mock
    private FilterChain filterChain;

    private ApiKeyAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        filter = new ApiKeyAuthenticationFilter(apiKeyService);
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void authenticatesAValidApiKeyBearerToken() throws Exception {
        var principal = new ApiKeyPrincipal("key-1", "house-1", HouseRole.OWNER);
        when(apiKeyService.authenticate("rcpl_abc123")).thenReturn(Optional.of(principal));
        var request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer rcpl_abc123");
        var response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication, is(notNullValue()));
        assertThat(authentication.getPrincipal(), is(principal));
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void leavesTheSecurityContextUntouchedForAnInvalidApiKey() throws Exception {
        when(apiKeyService.authenticate("rcpl_bad")).thenReturn(Optional.empty());
        var request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer rcpl_bad");

        filter.doFilter(request, new MockHttpServletResponse(), filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication(), is(nullValue()));
    }

    @Test
    void ignoresNonApiKeyBearerTokens() throws Exception {
        var request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer eyJhbGciOiJIUzI1NiJ9.some.jwt");

        filter.doFilter(request, new MockHttpServletResponse(), filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication(), is(nullValue()));
        verifyNoInteractions(apiKeyService);
    }

    @Test
    void ignoresRequestsWithNoAuthorizationHeader() throws Exception {
        filter.doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(), filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication(), is(nullValue()));
        verifyNoInteractions(apiKeyService);
    }

    @Test
    void ignoresNonBearerAuthorizationHeaders() throws Exception {
        var request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Basic dXNlcjpwYXNz");

        filter.doFilter(request, new MockHttpServletResponse(), filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication(), is(nullValue()));
        verifyNoInteractions(apiKeyService);
    }
}
