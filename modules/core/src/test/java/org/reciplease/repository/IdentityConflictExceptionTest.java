package org.reciplease.repository;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

class IdentityConflictExceptionTest {

    @Test
    void messageIncludesTheProviderAndProviderId() {
        var exception = new IdentityConflictException("google", "google-sub-1");

        assertThat(exception.getMessage(), containsString("google"));
        assertThat(exception.getMessage(), containsString("google-sub-1"));
    }
}
