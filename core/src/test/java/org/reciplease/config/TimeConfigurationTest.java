package org.reciplease.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class TimeConfigurationTest {
    @Test
    @DisplayName("should return UTC clock")
    void utcClock() {
        final var timeConfiguration = new TimeConfiguration();

        assertThat(timeConfiguration.clock(), is(Clock.systemUTC()));
    }
}