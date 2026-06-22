package org.reciplease.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.reciplease.model.UserIdentityDocument;
import org.reciplease.repository.mongo.UserIdentityMongoRepository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;

@DataMongoTest
@Import(UserIdentityRepositoryImpl.class)
class UserIdentityRepositoryImplTest {
    @Autowired
    private UserIdentityRepository userIdentityRepository;
    @Autowired
    private UserIdentityMongoRepository userIdentityMongoRepository;
    @Autowired
    private MongoTemplate mongoTemplate;

    @BeforeEach
    void cleanDatabase() {
        mongoTemplate.getCollectionNames().forEach(mongoTemplate::dropCollection);
    }

    @Test
    void findProvidersForUserReturnsTheProviderPartOfEachLinkedIdentity() {
        userIdentityMongoRepository.save(UserIdentityDocument.builder()
                .id(UserIdentityDocument.idFor("google", "google-sub-1")).userId("user-1").build());
        userIdentityMongoRepository.save(UserIdentityDocument.builder()
                .id(UserIdentityDocument.idFor("github", "github-sub-1")).userId("user-1").build());
        userIdentityMongoRepository.save(UserIdentityDocument.builder()
                .id(UserIdentityDocument.idFor("google", "google-sub-2")).userId("user-2").build());

        var providers = userIdentityRepository.findProvidersForUser("user-1");

        assertThat(providers, contains("google", "github"));
    }

    @Test
    void findProvidersForUserReturnsEmptyWhenNoneLinked() {
        var providers = userIdentityRepository.findProvidersForUser("unknown-user");

        assertThat(providers, empty());
    }

    @Test
    void findProvidersForUserFallsBackToTheWholeIdWhenThereIsNoColon() {
        userIdentityMongoRepository.save(UserIdentityDocument.builder()
                .id("no-separator-id").userId("user-1").build());

        var providers = userIdentityRepository.findProvidersForUser("user-1");

        assertThat(providers, contains("no-separator-id"));
    }
}
