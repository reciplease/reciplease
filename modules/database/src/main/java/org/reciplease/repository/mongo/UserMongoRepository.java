package org.reciplease.repository.mongo;

import org.reciplease.model.UserDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserMongoRepository extends MongoRepository<UserDocument, String> {
    Optional<UserDocument> findByHandle(String handle);
}
