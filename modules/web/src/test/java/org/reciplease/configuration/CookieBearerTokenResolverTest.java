package org.reciplease.configuration;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

class CookieBearerTokenResolverTest {

    private final CookieBearerTokenResolver resolver = new CookieBearerTokenResolver();

    @Test
    void resolvesTheTokenFromTheSessionCookie() {
        final var request = new MockHttpServletRequest();
        request.setCookies(new Cookie("reciplease-session", "cookie-token"));

        assertThat(resolver.resolve(request), is("cookie-token"));
    }

    @Test
    void fallsBackToTheAuthorizationHeaderWhenNoCookieIsPresent() {
        final var request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer header-token");

        assertThat(resolver.resolve(request), is("header-token"));
    }

    @Test
    void prefersTheCookieOverAnAuthorizationHeader() {
        final var request = new MockHttpServletRequest();
        request.setCookies(new Cookie("reciplease-session", "cookie-token"));
        request.addHeader("Authorization", "Bearer header-token");

        assertThat(resolver.resolve(request), is("cookie-token"));
    }

    @Test
    void returnsNullWhenNeitherCookieNorHeaderIsPresent() {
        final var request = new MockHttpServletRequest();

        assertThat(resolver.resolve(request), nullValue());
    }

    @Test
    void returnsNullForAnApiKeyBearerHeaderRatherThanHandingItToTheJwtDecoder() {
        final var request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer rcpl_someapikey1234567890");

        assertThat(resolver.resolve(request), nullValue());
    }

    @Test
    void returnsNullForAnApiKeyInTheSessionCookieToo() {
        final var request = new MockHttpServletRequest();
        request.setCookies(new Cookie("reciplease-session", "rcpl_someapikey1234567890"));

        assertThat(resolver.resolve(request), nullValue());
    }
}
