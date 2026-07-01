package org.reciplease.configuration;

import io.mongock.runner.springboot.base.MongockApplicationRunner;
import org.junit.jupiter.api.Test;
import org.reciplease.model.InviteDocument;
import org.reciplease.model.PlannedMealDocument;
import org.reciplease.model.UserDocument;
import org.reciplease.model.WebAuthnChallengeDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;

/**
 * Verifies the Mongock changesets under {@code org.reciplease.migration} actually run and
 * create their indexes — {@code @DataMongoTest}'s context doesn't invoke {@code ApplicationRunner}
 * beans the way a real {@code SpringApplication.run()} would, so the runner is triggered manually.
 */
@DataMongoTest
@Import(MongockConfig.class)
class MongockConfigTest {

    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private MongockApplicationRunner mongockApplicationRunner;

    @Test
    void createsTheUserHandleUniqueIndex() throws Exception {
        mongockApplicationRunner.run(new DefaultApplicationArguments());

        final var indexFields = mongoTemplate.indexOps(UserDocument.class).getIndexInfo().stream()
                .map(info -> info.getIndexFields().toString())
                .toList();
        assertThat(indexFields, hasItem(containsString("handle")));
    }

    @Test
    void createsTheInviteCodeUniqueIndex() throws Exception {
        mongockApplicationRunner.run(new DefaultApplicationArguments());

        final var indexFields = mongoTemplate.indexOps(InviteDocument.class).getIndexInfo().stream()
                .map(info -> info.getIndexFields().toString())
                .toList();
        assertThat(indexFields, hasItem(containsString("code")));
    }

    @Test
    void createsTheWebAuthnChallengeTtlIndex() throws Exception {
        mongockApplicationRunner.run(new DefaultApplicationArguments());

        final var indexes = mongoTemplate.indexOps(WebAuthnChallengeDocument.class).getIndexInfo();
        assertThat(indexes.stream().anyMatch(info -> info.getExpireAfter().isPresent()), is(true));
    }

    @Test
    void createsThePlannedMealNameUniqueIndex() throws Exception {
        mongockApplicationRunner.run(new DefaultApplicationArguments());

        final var indexFields = mongoTemplate.indexOps(PlannedMealDocument.class).getIndexInfo().stream()
                .map(info -> info.getIndexFields().toString())
                .toList();
        assertThat(indexFields, hasItem(containsString("name")));
    }

    @Test
    void runningTheMigrationsTwiceInARowDoesNotFail() throws Exception {
        // Every app restart re-runs this — Mongock must skip changesets it's already applied.
        mongockApplicationRunner.run(new DefaultApplicationArguments());

        mongockApplicationRunner.run(new DefaultApplicationArguments());
    }
}
