package org.reciplease.repository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

import org.junit.jupiter.api.Test;

class IdentityConflictExceptionTest {

    @Test
    void messageIncludesTheProviderAndProviderId() {
        var exception = new IdentityConflictException("google", "google-sub-1");

        assertThat(exception.getMessage(), containsString("google"));
        assertThat(exception.getMessage(), containsString("google-sub-1"));
    }
}
