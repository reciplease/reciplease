package org.reciplease.repository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reciplease.model.LinkedIdentity;
import org.reciplease.model.PasskeyCredentialDocument;
import org.reciplease.model.UserIdentityDocument;
import org.reciplease.repository.mongo.PasskeyCredentialMongoRepository;
import org.reciplease.repository.mongo.UserIdentityMongoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;

@DataMongoTest
@Import(UserIdentityRepositoryImpl.class)
class UserIdentityRepositoryImplTest {
    @Autowired
    private UserIdentityRepository userIdentityRepository;

    @Autowired
    private UserIdentityMongoRepository userIdentityMongoRepository;

    @Autowired
    private PasskeyCredentialMongoRepository passkeyCredentialMongoRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @BeforeEach
    void cleanDatabase() {
        mongoTemplate.getCollectionNames().forEach(mongoTemplate::dropCollection);
    }

    @Test
    void findIdentitiesForUserReturnsTheProviderAndEmailOfEachLinkedIdentity() {
        userIdentityMongoRepository.save(UserIdentityDocument.builder()
                .id(UserIdentityDocument.idFor("google", "google-sub-1"))
                .userId("user-1")
                .email("user1@gmail.com")
                .build());
        userIdentityMongoRepository.save(UserIdentityDocument.builder()
                .id(UserIdentityDocument.idFor("github", "github-sub-1"))
                .userId("user-1")
                .email("user1@github.com")
                .build());
        userIdentityMongoRepository.save(UserIdentityDocument.builder()
                .id(UserIdentityDocument.idFor("google", "google-sub-2"))
                .userId("user-2")
                .email("user2@gmail.com")
                .build());

        var identities = userIdentityRepository.findIdentitiesForUser("user-1");

        assertThat(
                identities,
                contains(
                        new LinkedIdentity("google", "user1@gmail.com"),
                        new LinkedIdentity("github", "user1@github.com")));
    }

    @Test
    void findIdentitiesForUserAllowsANullEmail() {
        userIdentityMongoRepository.save(UserIdentityDocument.builder()
                .id(UserIdentityDocument.idFor("google", "google-sub-1"))
                .userId("user-1")
                .build());

        var identities = userIdentityRepository.findIdentitiesForUser("user-1");

        assertThat(identities, contains(new LinkedIdentity("google", null)));
    }

    @Test
    void findIdentitiesForUserReturnsEmptyWhenNoneLinked() {
        var identities = userIdentityRepository.findIdentitiesForUser("unknown-user");

        assertThat(identities, empty());
    }

    @Test
    void findIdentitiesForUserFallsBackToTheWholeIdWhenThereIsNoColon() {
        userIdentityMongoRepository.save(UserIdentityDocument.builder()
                .id("no-separator-id")
                .userId("user-1")
                .build());

        var identities = userIdentityRepository.findIdentitiesForUser("user-1");

        assertThat(identities, contains(new LinkedIdentity("no-separator-id", null)));
    }

    @Test
    void removeForUserDeletesOnlyThatUsersIdentityForThatProvider() {
        userIdentityMongoRepository.save(UserIdentityDocument.builder()
                .id(UserIdentityDocument.idFor("google", "google-sub-1"))
                .userId("user-1")
                .email("user1@gmail.com")
                .build());
        userIdentityMongoRepository.save(UserIdentityDocument.builder()
                .id(UserIdentityDocument.idFor("github", "github-sub-1"))
                .userId("user-1")
                .email("user1@github.com")
                .build());
        userIdentityMongoRepository.save(UserIdentityDocument.builder()
                .id(UserIdentityDocument.idFor("github", "github-sub-2"))
                .userId("user-2")
                .email("user2@github.com")
                .build());

        userIdentityRepository.removeForUser("user-1", "github");

        assertThat(
                userIdentityRepository.findIdentitiesForUser("user-1"),
                contains(new LinkedIdentity("google", "user1@gmail.com")));
        // Another user's github identity is untouched.
        assertThat(
                userIdentityRepository.findIdentitiesForUser("user-2"),
                contains(new LinkedIdentity("github", "user2@github.com")));
    }

    @Test
    void removeForUserAlsoDeletesThatUsersPasskeyCredentialsSoTheyCantStillSignIn() {
        userIdentityMongoRepository.save(UserIdentityDocument.builder()
                .id(UserIdentityDocument.idFor("passkey", "credential-1"))
                .userId("user-1")
                .build());
        passkeyCredentialMongoRepository.save(PasskeyCredentialDocument.builder()
                .id("credential-1")
                .userId("user-1")
                .transports(java.util.Set.of())
                .build());
        passkeyCredentialMongoRepository.save(PasskeyCredentialDocument.builder()
                .id("credential-2")
                .userId("user-2")
                .transports(java.util.Set.of())
                .build());

        userIdentityRepository.removeForUser("user-1", "passkey");

        assertThat(passkeyCredentialMongoRepository.findAllByUserId("user-1"), empty());
        // Another user's credential is untouched.
        assertThat(passkeyCredentialMongoRepository.findAllByUserId("user-2"), org.hamcrest.Matchers.hasSize(1));
    }

    @Test
    void removeForUserDoesNotTouchPasskeyCredentialsWhenRemovingADifferentProvider() {
        userIdentityMongoRepository.save(UserIdentityDocument.builder()
                .id(UserIdentityDocument.idFor("google", "google-sub-1"))
                .userId("user-1")
                .build());
        passkeyCredentialMongoRepository.save(PasskeyCredentialDocument.builder()
                .id("credential-1")
                .userId("user-1")
                .transports(java.util.Set.of())
                .build());

        userIdentityRepository.removeForUser("user-1", "google");

        assertThat(passkeyCredentialMongoRepository.findAllByUserId("user-1"), org.hamcrest.Matchers.hasSize(1));
    }
}
