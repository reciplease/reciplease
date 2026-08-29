package org.reciplease.configuration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import org.junit.jupiter.api.Test;
import org.reciplease.model.ApiKeyPrincipal;
import org.reciplease.model.HouseRole;

class ApiKeyAuthenticationTokenTest {

    @Test
    void carriesTheApiKeyIdAsItsNameAndTheRoleGrantingAuthorityOnly() {
        var principal = new ApiKeyPrincipal("key-1", "house-1", HouseRole.OWNER);
        var token = new ApiKeyAuthenticationToken(principal);

        assertThat(token.getName(), is("apikey:key-1"));
        assertThat(token.getPrincipal(), is(principal));
        assertThat(token.getCredentials(), is(nullValue()));
        assertThat(token.isAuthenticated(), is(true));
        assertThat(token.getAuthorities().stream().map(Object::toString).toList(), contains("ROLE_RECIPLEASE"));
    }
}
