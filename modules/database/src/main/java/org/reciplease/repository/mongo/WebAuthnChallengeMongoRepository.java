package org.reciplease.repository.mongo;

import org.reciplease.model.WebAuthnChallengeDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface WebAuthnChallengeMongoRepository extends MongoRepository<WebAuthnChallengeDocument, String> {
}
