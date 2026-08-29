package org.reciplease.configuration;

import java.util.List;
import org.reciplease.model.ApiKeyPrincipal;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * The authenticated identity for a request bearing a valid house service-account API key (see
 * {@link ApiKeyAuthenticationFilter}). Always carries {@code ROLE_RECIPLEASE} — a key is only
 * ever minted for a house that already exists, so unlike a user JWT there is no separate
 * membership check to fail. {@link HouseAccess} reads {@link #getPrincipal()} directly rather
 * than consulting {@code HouseRepository}, since the key's house/role assignment *is* the
 * authorization, not a lookup key into one.
 */
public class ApiKeyAuthenticationToken extends AbstractAuthenticationToken {

    private final ApiKeyPrincipal principal;

    public ApiKeyAuthenticationToken(final ApiKeyPrincipal principal) {
        super(List.of(new SimpleGrantedAuthority("ROLE_RECIPLEASE")));
        this.principal = principal;
        setAuthenticated(true);
    }

    @Override
    public ApiKeyPrincipal getPrincipal() {
        return principal;
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public String getName() {
        return "apikey:" + principal.apiKeyId();
    }
}
