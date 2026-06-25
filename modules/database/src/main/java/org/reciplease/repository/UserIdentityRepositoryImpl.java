package org.reciplease.repository;

import lombok.RequiredArgsConstructor;
import org.reciplease.model.LinkedIdentity;
import org.reciplease.model.UserIdentityDocument;
import org.reciplease.repository.mongo.PasskeyCredentialMongoRepository;
import org.reciplease.repository.mongo.UserIdentityMongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class UserIdentityRepositoryImpl implements UserIdentityRepository {

    private static final String PASSKEY_PROVIDER = "passkey";

    private final UserIdentityMongoRepository userIdentityMongoRepository;
    private final PasskeyCredentialMongoRepository passkeyCredentialMongoRepository;

    @Override
    public List<LinkedIdentity> findIdentitiesForUser(final String userId) {
        return userIdentityMongoRepository.findAllByUserId(userId).stream()
                .map(identity -> new LinkedIdentity(providerOf(identity), identity.getEmail()))
                .toList();
    }

    @Override
    public void removeForUser(final String userId, final String provider) {
        userIdentityMongoRepository.findAllByUserId(userId).stream()
                .filter(identity -> providerOf(identity).equals(provider))
                .forEach(identity -> userIdentityMongoRepository.deleteById(identity.getId()));
        // Unlinking "passkey" means removing every registered credential, not just the
        // identity-listing row — otherwise an "unlinked" passkey could still sign in.
        if (PASSKEY_PROVIDER.equals(provider)) {
            passkeyCredentialMongoRepository.deleteAllByUserId(userId);
        }
    }

    private static String providerOf(final UserIdentityDocument identity) {
        final var id = identity.getId();
        final var separator = id.indexOf(':');
        return separator >= 0 ? id.substring(0, separator) : id;
    }
}
