package org.reciplease.configuration;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAccessor;
import java.util.Optional;
import org.springframework.data.auditing.DateTimeProvider;

/**
 * MongoDB stores {@link Instant}s with millisecond precision, so auditing timestamps are
 * truncated to milliseconds when set. This keeps a freshly-saved entity equal to one
 * re-fetched from the database.
 */
class ClockAuditingDateTimeProvider implements DateTimeProvider {

    private final Clock clock;

    ClockAuditingDateTimeProvider(final Clock clock) {
        this.clock = clock;
    }

    @Override
    public Optional<TemporalAccessor> getNow() {
        return Optional.of(Instant.now(clock).truncatedTo(ChronoUnit.MILLIS));
    }
}
