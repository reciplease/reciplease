package org.reciplease.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reciplease.model.PasskeyUserHandles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.web.webauthn.api.AuthenticatorTransport;
import org.springframework.security.web.webauthn.api.Bytes;
import org.springframework.security.web.webauthn.api.ImmutableCredentialRecord;
import org.springframework.security.web.webauthn.api.ImmutablePublicKeyCose;
import org.springframework.security.web.webauthn.api.PublicKeyCredentialType;

import java.time.Instant;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

@DataMongoTest
@Import(PasskeyUserCredentialRepositoryImpl.class)
class PasskeyUserCredentialRepositoryImplTest {

    @Autowired
    private PasskeyUserCredentialRepositoryImpl repository;
    @Autowired
    private MongoTemplate mongoTemplate;

    @BeforeEach
    void cleanDatabase() {
        mongoTemplate.getCollectionNames().forEach(mongoTemplate::dropCollection);
    }

    private static ImmutableCredentialRecord credentialFor(final String credentialId, final String userId) {
        return ImmutableCredentialRecord.builder()
                .credentialType(PublicKeyCredentialType.PUBLIC_KEY)
                .credentialId(Bytes.fromBase64(credentialId))
                .userEntityUserId(PasskeyUserHandles.toHandle(userId))
                .publicKey(new ImmutablePublicKeyCose(new byte[]{1, 2, 3}))
                .signatureCount(0)
                .uvInitialized(true)
                .transports(Set.of(AuthenticatorTransport.INTERNAL))
                .backupEligible(true)
                .backupState(false)
                .label("My device")
                .created(Instant.now())
                .lastUsed(Instant.now())
                .build();
    }

    @Test
    void saveThenFindByCredentialIdRoundTripsTheCredential() {
        final var credential = credentialFor("aGVsbG8", "user-1");
        repository.save(credential);

        final var found = repository.findByCredentialId(credential.getCredentialId());

        assertThat(found.getCredentialId(), is(credential.getCredentialId()));
        assertThat(found.getUserEntityUserId(), is(credential.getUserEntityUserId()));
        assertThat(found.getPublicKey().getBytes(), is(credential.getPublicKey().getBytes()));
        assertThat(found.getSignatureCount(), is(0L));
        assertThat(found.isUvInitialized(), is(true));
        assertThat(found.getTransports(), contains(AuthenticatorTransport.INTERNAL));
        assertThat(found.isBackupEligible(), is(true));
        assertThat(found.isBackupState(), is(false));
        assertThat(found.getLabel(), is("My device"));
    }

    @Test
    void findByCredentialIdReturnsNullWhenUnknown() {
        assertThat(repository.findByCredentialId(Bytes.random()), nullValue());
    }

    @Test
    void findByUserIdReturnsAllOfThatUsersCredentials() {
        repository.save(credentialFor("aGVsbG8x", "user-1"));
        repository.save(credentialFor("aGVsbG8y", "user-1"));
        repository.save(credentialFor("aGVsbG8z", "user-2"));

        final var found = repository.findByUserId(PasskeyUserHandles.toHandle("user-1"));

        assertThat(found.size(), is(2));
    }

    @Test
    void findByUserIdReturnsEmptyWhenNoneRegistered() {
        assertThat(repository.findByUserId(PasskeyUserHandles.toHandle("unknown-user")), empty());
    }

    @Test
    void deleteRemovesOnlyThatCredential() {
        final var keep = credentialFor("aGVsbG8x", "user-1");
        final var remove = credentialFor("aGVsbG8y", "user-1");
        repository.save(keep);
        repository.save(remove);

        repository.delete(remove.getCredentialId());

        assertThat(repository.findByCredentialId(remove.getCredentialId()), nullValue());
        assertThat(repository.findByCredentialId(keep.getCredentialId()), notNullValue());
    }
}
