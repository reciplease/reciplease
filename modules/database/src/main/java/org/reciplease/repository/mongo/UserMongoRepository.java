package org.reciplease.repository.mongo;

import java.util.Optional;
import org.reciplease.model.UserDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserMongoRepository extends MongoRepository<UserDocument, String> {
    Optional<UserDocument> findByHandle(String handle);
}
