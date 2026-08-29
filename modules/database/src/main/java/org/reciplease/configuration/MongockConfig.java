package org.reciplease.configuration;

import io.mongock.api.config.MongockConfiguration;
import io.mongock.driver.mongodb.springdata.v4.SpringDataMongoV4Driver;
import io.mongock.runner.springboot.MongockSpringboot;
import io.mongock.runner.springboot.base.MongockApplicationRunner;
import java.util.List;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

/**
 * Runs versioned migration changesets (see {@code org.reciplease.migration}) on every startup —
 * the actual mechanism behind the index/schema changes {@code @Indexed} et al. only declare.
 * {@code spring.data.mongodb.auto-index-creation} handles a {@code @Indexed} annotation's index
 * existing in the first place; it can't safely change one that's already there (a redefinition
 * can fail outright at startup), which is what a real, ordered changeset is for.
 */
@Configuration
public class MongockConfig {

    @Bean
    public MongockApplicationRunner mongockApplicationRunner(
            final ApplicationContext context, final MongoTemplate mongoTemplate) {
        final var config = new MongockConfiguration();
        config.setMigrationScanPackage(List.of("org.reciplease.migration"));

        return MongockSpringboot.builder()
                .setDriver(SpringDataMongoV4Driver.withDefaultLock(mongoTemplate))
                .setSpringContext(context)
                .setConfig(config)
                .buildApplicationRunner();
    }
}
