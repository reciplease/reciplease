package org.reciplease.repository;

import com.mongodb.DuplicateKeyException;
import lombok.RequiredArgsConstructor;
import org.reciplease.model.User;
import org.reciplease.model.UserDocument;
import org.reciplease.model.UserIdentityDocument;
import org.reciplease.repository.mongo.UserIdentityMongoRepository;
import org.reciplease.repository.mongo.UserMongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {
    private final UserMongoRepository userMongoRepository;
    private final UserIdentityMongoRepository userIdentityMongoRepository;

    @Override
    public User save(final User user) {
        return userMongoRepository.save(UserDocument.from(user)).toModel();
    }

    @Override
    public Optional<User> findById(final String id) {
        return userMongoRepository.findById(id).map(UserDocument::toModel);
    }

    @Override
    public Optional<User> findByIdentity(final String provider, final String providerId) {
        return userIdentityMongoRepository.findById(UserIdentityDocument.idFor(provider, providerId))
                .flatMap(identity -> userMongoRepository.findById(identity.getUserId()))
                .map(UserDocument::toModel);
    }

    @Override
    public User createWithIdentity(final String provider, final String providerId, final String email) {
        final var userId = UUID.randomUUID().toString();
        final var user = userMongoRepository.save(UserDocument.builder().id(userId).handle(null).build()).toModel();
        try {
            userIdentityMongoRepository.save(UserIdentityDocument.builder()
                    .id(UserIdentityDocument.idFor(provider, providerId))
                    .userId(userId)
                    .email(email)
                    .build());
        } catch (final DuplicateKeyException e) {
            throw new IdentityConflictException(provider, providerId);
        }
        return user;
    }

    @Override
    public void linkIdentity(final String userId, final String provider, final String providerId, final String email) {
        final var identityId = UserIdentityDocument.idFor(provider, providerId);
        final var existing = userIdentityMongoRepository.findById(identityId);
        if (existing.isPresent()) {
            if (!existing.get().getUserId().equals(userId)) {
                throw new IdentityConflictException(provider, providerId);
            }
            return;
        }
        try {
            userIdentityMongoRepository.save(UserIdentityDocument.builder()
                    .id(identityId)
                    .userId(userId)
                    .email(email)
                    .build());
        } catch (final DuplicateKeyException e) {
            throw new IdentityConflictException(provider, providerId);
        }
    }

    @Override
    public void setHandle(final String userId, final String handle) {
        final var holder = userMongoRepository.findByHandle(handle);
        if (holder.isPresent() && !holder.get().getId().equals(userId)) {
            throw new HandleTakenException(handle);
        }
        userMongoRepository.save(UserDocument.builder().id(userId).handle(handle).build());
    }
}
