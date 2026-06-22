package org.reciplease.repository.mongo;

import org.reciplease.model.UserIdentityDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface UserIdentityMongoRepository extends MongoRepository<UserIdentityDocument, String> {
    List<UserIdentityDocument> findAllByUserId(String userId);
}
