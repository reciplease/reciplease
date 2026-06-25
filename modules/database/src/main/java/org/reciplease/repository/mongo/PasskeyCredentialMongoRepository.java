package org.reciplease.repository.mongo;

import org.reciplease.model.PasskeyCredentialDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface PasskeyCredentialMongoRepository extends MongoRepository<PasskeyCredentialDocument, String> {
    List<PasskeyCredentialDocument> findAllByUserId(String userId);

    void deleteAllByUserId(String userId);
}
