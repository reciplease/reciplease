package org.reciplease.repository;

import lombok.RequiredArgsConstructor;
import org.reciplease.model.UserIdentityDocument;
import org.reciplease.repository.mongo.UserIdentityMongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class UserIdentityRepositoryImpl implements UserIdentityRepository {
    private final UserIdentityMongoRepository userIdentityMongoRepository;

    @Override
    public List<String> findProvidersForUser(final String userId) {
        return userIdentityMongoRepository.findAllByUserId(userId).stream()
                .map(UserIdentityRepositoryImpl::providerOf)
                .toList();
    }

    private static String providerOf(final UserIdentityDocument identity) {
        final var id = identity.getId();
        final var separator = id.indexOf(':');
        return separator >= 0 ? id.substring(0, separator) : id;
    }
}
