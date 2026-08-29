package org.reciplease.config;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.time.Clock;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TimeConfigurationTest {
    @Test
    @DisplayName("should return UTC clock")
    void utcClock() {
        final var timeConfiguration = new TimeConfiguration();

        assertThat(timeConfiguration.clock(), is(Clock.systemUTC()));
    }
}
