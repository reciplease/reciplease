package org.reciplease.repository;

import org.reciplease.model.PasskeyUserHandles;
import org.springframework.security.web.webauthn.api.Bytes;
import org.springframework.security.web.webauthn.api.ImmutablePublicKeyCredentialUserEntity;
import org.springframework.security.web.webauthn.api.PublicKeyCredentialUserEntity;
import org.springframework.security.web.webauthn.management.PublicKeyCredentialUserEntityRepository;
import org.springframework.stereotype.Repository;

/**
 * WebAuthn's "username" is the Reciplease user id (see {@link PasskeyUserHandles}), so the
 * user entity is derived on the fly rather than persisted — there's nothing here that isn't
 * already recoverable from the id/username itself.
 */
@Repository
public class PasskeyUserEntityRepositoryImpl implements PublicKeyCredentialUserEntityRepository {

    @Override
    public PublicKeyCredentialUserEntity findById(final Bytes id) {
        return toUserEntity(PasskeyUserHandles.toUserId(id));
    }

    @Override
    public PublicKeyCredentialUserEntity findByUsername(final String username) {
        return toUserEntity(username);
    }

    @Override
    public void save(final PublicKeyCredentialUserEntity userEntity) {
        // No-op: nothing to persist beyond the id/username, which already round-trip losslessly.
    }

    @Override
    public void delete(final Bytes id) {
        // No-op, as above.
    }

    private static PublicKeyCredentialUserEntity toUserEntity(final String userId) {
        return ImmutablePublicKeyCredentialUserEntity.builder()
                .name(userId)
                .id(PasskeyUserHandles.toHandle(userId))
                .displayName(userId)
                .build();
    }
}
