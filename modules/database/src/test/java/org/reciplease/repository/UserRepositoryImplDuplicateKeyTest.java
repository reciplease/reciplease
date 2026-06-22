package org.reciplease.repository;

import com.mongodb.DuplicateKeyException;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.reciplease.repository.mongo.UserIdentityMongoRepository;
import org.reciplease.repository.mongo.UserMongoRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * {@link UserRepositoryTest} covers the happy paths against a real (embedded) Mongo;
 * these two race-condition branches — a concurrent insert winning between our
 * existence check and our own insert — are only reachable by mocking the underlying
 * Mongo repositories to simulate the duplicate-key error a real race would produce.
 */
@MockitoSettings
class UserRepositoryImplDuplicateKeyTest {

    @Mock
    private UserMongoRepository userMongoRepository;
    @Mock
    private UserIdentityMongoRepository userIdentityMongoRepository;

    private UserRepositoryImpl userRepository;

    @Test
    void createWithIdentityThrowsConflictWhenTheIdentityIsInsertedConcurrently() {
        userRepository = new UserRepositoryImpl(userMongoRepository, userIdentityMongoRepository);
        when(userMongoRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(userIdentityMongoRepository.save(any())).thenThrow(mock(DuplicateKeyException.class));

        assertThrows(IdentityConflictException.class,
                () -> userRepository.createWithIdentity("google", "google-sub-1"));
    }

    @Test
    void linkIdentityThrowsConflictWhenTheIdentityIsInsertedConcurrently() {
        userRepository = new UserRepositoryImpl(userMongoRepository, userIdentityMongoRepository);
        when(userIdentityMongoRepository.findById(any())).thenReturn(Optional.empty());
        when(userIdentityMongoRepository.save(any())).thenThrow(mock(DuplicateKeyException.class));

        assertThrows(IdentityConflictException.class,
                () -> userRepository.linkIdentity("user-1", "google", "google-sub-1"));
    }
}
