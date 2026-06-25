package org.reciplease.repository;

import lombok.RequiredArgsConstructor;
import org.reciplease.model.PasskeyCredentialDocument;
import org.reciplease.model.PasskeyUserHandles;
import org.reciplease.repository.mongo.PasskeyCredentialMongoRepository;
import org.springframework.security.web.webauthn.api.Bytes;
import org.springframework.security.web.webauthn.api.CredentialRecord;
import org.springframework.security.web.webauthn.management.UserCredentialRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class PasskeyUserCredentialRepositoryImpl implements UserCredentialRepository {

    private final PasskeyCredentialMongoRepository passkeyCredentialMongoRepository;

    @Override
    public void save(final CredentialRecord credentialRecord) {
        final var userId = PasskeyUserHandles.toUserId(credentialRecord.getUserEntityUserId());
        passkeyCredentialMongoRepository.save(PasskeyCredentialDocument.from(credentialRecord, userId, credentialRecord.getLabel()));
    }

    @Override
    public CredentialRecord findByCredentialId(final Bytes credentialId) {
        return passkeyCredentialMongoRepository.findById(credentialId.toBase64UrlString())
                .map(PasskeyCredentialDocument::toCredentialRecord)
                .orElse(null);
    }

    @Override
    public List<CredentialRecord> findByUserId(final Bytes userId) {
        return passkeyCredentialMongoRepository.findAllByUserId(PasskeyUserHandles.toUserId(userId)).stream()
                .map(PasskeyCredentialDocument::toCredentialRecord)
                .map(CredentialRecord.class::cast)
                .toList();
    }

    @Override
    public void delete(final Bytes credentialId) {
        passkeyCredentialMongoRepository.deleteById(credentialId.toBase64UrlString());
    }
}
