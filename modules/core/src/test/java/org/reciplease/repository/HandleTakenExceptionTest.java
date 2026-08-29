package org.reciplease.repository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

import org.junit.jupiter.api.Test;

class HandleTakenExceptionTest {

    @Test
    void messageIncludesTheTakenHandle() {
        var exception = new HandleTakenException("taken-handle");

        assertThat(exception.getMessage(), containsString("taken-handle"));
    }
}
