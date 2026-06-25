package org.reciplease.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@DataMongoTest
@Import(WebAuthnChallengeLedgerImpl.class)
class WebAuthnChallengeLedgerImplTest {

    @Autowired
    private WebAuthnChallengeLedger challengeLedger;
    @Autowired
    private MongoTemplate mongoTemplate;

    @BeforeEach
    void cleanDatabase() {
        mongoTemplate.getCollectionNames().forEach(mongoTemplate::dropCollection);
    }

    @Test
    void consumeForRegistrationReturnsTheUserIdExactlyOnceForAnIssuedChallenge() {
        challengeLedger.issue("challenge-1", "user-1");

        assertThat(challengeLedger.consumeForRegistration("challenge-1"), is(Optional.of("user-1")));
        assertThat(challengeLedger.consumeForRegistration("challenge-1"), is(Optional.empty()));
    }

    @Test
    void consumeForRegistrationReturnsEmptyForAChallengeThatWasNeverIssued() {
        assertThat(challengeLedger.consumeForRegistration("never-issued"), is(Optional.empty()));
    }

    @Test
    void consumeForLoginSucceedsExactlyOnceForAnIssuedChallenge() {
        challengeLedger.issue("challenge-1", "user-1");

        assertThat(challengeLedger.consumeForLogin("challenge-1"), is(true));
        assertThat(challengeLedger.consumeForLogin("challenge-1"), is(false));
    }

    @Test
    void consumeForLoginFailsForAChallengeThatWasNeverIssued() {
        assertThat(challengeLedger.consumeForLogin("never-issued"), is(false));
    }
}
