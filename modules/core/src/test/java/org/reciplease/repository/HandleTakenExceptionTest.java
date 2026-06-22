package org.reciplease.repository;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

class HandleTakenExceptionTest {

    @Test
    void messageIncludesTheTakenHandle() {
        var exception = new HandleTakenException("taken-handle");

        assertThat(exception.getMessage(), containsString("taken-handle"));
    }
}
