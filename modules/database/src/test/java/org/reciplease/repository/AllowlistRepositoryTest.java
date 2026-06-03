package org.reciplease.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;

@DataMongoTest
@Import(AllowlistRepositoryImpl.class)
class AllowlistRepositoryTest {
    @Autowired
    private AllowlistRepository allowlistRepository;
    @Autowired
    private MongoTemplate mongoTemplate;

    @BeforeEach
    void cleanDatabase() {
        mongoTemplate.getCollectionNames().forEach(mongoTemplate::dropCollection);
    }

    @Test
    void containsIsTrueForAddedEmail() {
        allowlistRepository.add("rhys.saldanha@gmail.com");

        assertThat(allowlistRepository.contains("rhys.saldanha@gmail.com"), is(true));
    }

    @Test
    void containsIsFalseForUnknownEmail() {
        assertThat(allowlistRepository.contains("stranger@gmail.com"), is(false));
    }

    @Test
    void matchingIsCaseAndWhitespaceInsensitive() {
        allowlistRepository.add("  Rhys.Saldanha@Gmail.com  ");

        assertThat(allowlistRepository.contains("rhys.saldanha@gmail.com"), is(true));
        assertThat(allowlistRepository.findAll(), contains("rhys.saldanha@gmail.com"));
    }

    @Test
    void removeRevokesAccess() {
        allowlistRepository.add("rhys.saldanha@gmail.com");
        allowlistRepository.remove("RHYS.SALDANHA@gmail.com");

        assertThat(allowlistRepository.contains("rhys.saldanha@gmail.com"), is(false));
    }
}
