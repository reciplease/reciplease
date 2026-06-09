package org.reciplease.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reciplease.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@DataMongoTest
@Import(UserRepositoryImpl.class)
class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MongoTemplate mongoTemplate;

    @BeforeEach
    void cleanDatabase() {
        mongoTemplate.getCollectionNames().forEach(mongoTemplate::dropCollection);
    }

    @Test
    void savePersistsUser() {
        var user = new User("google-sub-123", "user@example.com");

        var saved = userRepository.save(user);

        assertThat(saved.id(), is("google-sub-123"));
        assertThat(saved.email(), is("user@example.com"));
    }

    @Test
    void saveIsIdempotentForSameGoogleId() {
        var user = new User("google-sub-123", "user@example.com");

        userRepository.save(user);
        var saved = userRepository.save(user);

        assertThat(saved.id(), is("google-sub-123"));
    }
}
