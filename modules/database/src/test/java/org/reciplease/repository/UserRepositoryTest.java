package org.reciplease.repository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reciplease.model.User;
import org.reciplease.model.UserIdentityDocument;
import org.reciplease.repository.mongo.UserIdentityMongoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;

@DataMongoTest
@Import(UserRepositoryImpl.class)
class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserIdentityMongoRepository userIdentityMongoRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @BeforeEach
    void cleanDatabase() {
        mongoTemplate.getCollectionNames().forEach(mongoTemplate::dropCollection);
    }

    @Test
    void savePersistsUser() {
        var user = new User("user-1", "handle-1");

        var saved = userRepository.save(user);

        assertThat(saved.id(), is("user-1"));
        assertThat(saved.handle(), is("handle-1"));
    }

    @Test
    void saveIsIdempotentForSameId() {
        var user = new User("user-1", "handle-1");

        userRepository.save(user);
        var saved = userRepository.save(user);

        assertThat(saved.id(), is("user-1"));
    }

    @Test
    void findByIdReturnsTheSavedUser() {
        userRepository.save(new User("user-1", "handle-1"));

        var found = userRepository.findById("user-1");

        assertThat(found.isPresent(), is(true));
        assertThat(found.get().handle(), is("handle-1"));
    }

    @Test
    void findByIdReturnsEmptyWhenMissing() {
        var found = userRepository.findById("missing");

        assertThat(found.isPresent(), is(false));
    }

    @Test
    void createWithIdentityCreatesANewUserWithNullHandleAndLinksTheIdentity() {
        var created = userRepository.createWithIdentity("google", "google-sub-1", "user1@gmail.com");

        assertThat(created.handle(), is(nullValue()));

        var found = userRepository.findByIdentity("google", "google-sub-1");
        assertThat(found.isPresent(), is(true));
        assertThat(found.get().id(), is(created.id()));
    }

    @Test
    void findByIdentityReturnsEmptyWhenNotLinked() {
        var found = userRepository.findByIdentity("google", "unknown-sub");

        assertThat(found.isPresent(), is(false));
    }

    @Test
    void linkIdentityLinksANewIdentityToAnExistingUser() {
        var user = userRepository.createWithIdentity("google", "google-sub-1", "user1@gmail.com");

        userRepository.linkIdentity(user.id(), "github", "github-sub-1", "user1@github.com");

        var found = userRepository.findByIdentity("github", "github-sub-1");
        assertThat(found.isPresent(), is(true));
        assertThat(found.get().id(), is(user.id()));
    }

    @Test
    void linkIdentityIsANoOpWhenAlreadyLinkedToTheSameUser() {
        var user = userRepository.createWithIdentity("google", "google-sub-1", "user1@gmail.com");

        userRepository.linkIdentity(user.id(), "google", "google-sub-1", "user1@gmail.com");

        var found = userRepository.findByIdentity("google", "google-sub-1");
        assertThat(found.get().id(), is(user.id()));
    }

    @Test
    void linkIdentityThrowsWhenAlreadyLinkedToADifferentUser() {
        var userA = userRepository.createWithIdentity("google", "google-sub-1", "user1@gmail.com");
        var userB = userRepository.createWithIdentity("github", "github-sub-1", "user2@github.com");

        assertThrows(
                IdentityConflictException.class,
                () -> userRepository.linkIdentity(userB.id(), "google", "google-sub-1", "user2@gmail.com"));
    }

    @Test
    void updateIdentityEmailUpdatesTheStoredEmailWithoutChangingTheLinkedUser() {
        var user = userRepository.createWithIdentity("google", "google-sub-1", "old@gmail.com");

        userRepository.updateIdentityEmail("google", "google-sub-1", "new@gmail.com");

        var identity = userIdentityMongoRepository.findById(UserIdentityDocument.idFor("google", "google-sub-1"));
        assertThat(identity.isPresent(), is(true));
        assertThat(identity.get().getEmail(), is("new@gmail.com"));
        assertThat(identity.get().getUserId(), is(user.id()));
    }

    @Test
    void updateIdentityEmailIsANoOpWhenTheIdentityIsntLinked() {
        userRepository.updateIdentityEmail("google", "unknown-sub", "new@gmail.com");

        var identity = userIdentityMongoRepository.findById(UserIdentityDocument.idFor("google", "unknown-sub"));
        assertThat(identity.isPresent(), is(false));
    }

    @Test
    void setHandleUpdatesTheUsersHandle() {
        userRepository.save(new User("user-1", null));

        userRepository.setHandle("user-1", "new-handle");

        var found = userRepository.findById("user-1");
        assertThat(found.get().handle(), is("new-handle"));
    }

    @Test
    void setHandleThrowsWhenAnotherUserAlreadyHasThatHandle() {
        userRepository.save(new User("user-1", "taken-handle"));
        userRepository.save(new User("user-2", null));

        assertThrows(HandleTakenException.class, () -> userRepository.setHandle("user-2", "taken-handle"));
    }
}
