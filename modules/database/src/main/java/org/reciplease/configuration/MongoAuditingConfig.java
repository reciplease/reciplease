package org.reciplease.configuration;

import org.reciplease.config.TimeConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

import java.time.Clock;

@Configuration
@EnableMongoAuditing(dateTimeProviderRef = "clockAuditingDateTimeProvider")
@Import(TimeConfiguration.class)
public class MongoAuditingConfig {

    @Bean
    public DateTimeProvider clockAuditingDateTimeProvider(final Clock clock) {
        return new ClockAuditingDateTimeProvider(clock);
    }
}
