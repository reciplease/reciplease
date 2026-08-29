package org.reciplease.repository.mongo;

import java.util.List;
import org.reciplease.model.PasskeyCredentialDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PasskeyCredentialMongoRepository extends MongoRepository<PasskeyCredentialDocument, String> {
    List<PasskeyCredentialDocument> findAllByUserId(String userId);

    void deleteAllByUserId(String userId);
}
