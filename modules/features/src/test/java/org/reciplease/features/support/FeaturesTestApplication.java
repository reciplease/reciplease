package org.reciplease.features.support;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

/**
 * Minimal {@code @SpringBootApplication}-equivalent that component-scans {@code core}, {@code web},
 * and {@code database} together without depending on the {@code dist} module (which also bundles
 * Cloud Run/Secret Manager concerns this suite doesn't need). {@code @EnableMethodSecurity} is
 * added here because the active (non-{@code cloud}) {@code WebSecurityConfig} doesn't enable it —
 * only {@code CloudWebSecurityConfig} does — but the {@code @HouseOwner}/{@code @HouseMember}
 * annotations under test need it regardless of which web security profile is active.
 */
@SpringBootConfiguration
@EnableAutoConfiguration
@EnableMethodSecurity
@ComponentScan(basePackages = "org.reciplease")
@EnableMongoRepositories(basePackages = "org.reciplease")
public class FeaturesTestApplication {
}
