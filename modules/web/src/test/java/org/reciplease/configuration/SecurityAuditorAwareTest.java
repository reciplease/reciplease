package org.reciplease.configuration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

class SecurityAuditorAwareTest {

    private final SecurityAuditorAware auditorAware = new SecurityAuditorAware();

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void returnsSubjectWhenAuthenticated() {
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken("google-sub-123", null, List.of()));

        assertThat(auditorAware.getCurrentAuditor().orElseThrow(), is("google-sub-123"));
    }

    @Test
    void returnsEmptyWhenNoAuthentication() {
        assertThat(auditorAware.getCurrentAuditor().isEmpty(), is(true));
    }
}
