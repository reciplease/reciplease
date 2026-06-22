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
    void containsIsTrueForAddedUserId() {
        allowlistRepository.add("user-1");

        assertThat(allowlistRepository.contains("user-1"), is(true));
    }

    @Test
    void containsIsFalseForUnknownUserId() {
        assertThat(allowlistRepository.contains("stranger"), is(false));
    }

    @Test
    void matchingIsExactSinceUserIdsAreOpaque() {
        allowlistRepository.add("user-1");

        assertThat(allowlistRepository.contains("user-1"), is(true));
        assertThat(allowlistRepository.findAll(), contains("user-1"));
    }

    @Test
    void removeRevokesAccess() {
        allowlistRepository.add("user-1");
        allowlistRepository.remove("user-1");

        assertThat(allowlistRepository.contains("user-1"), is(false));
    }
}
