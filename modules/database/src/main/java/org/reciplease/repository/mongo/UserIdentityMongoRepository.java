package org.reciplease.repository.mongo;

import java.util.List;
import org.reciplease.model.UserIdentityDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserIdentityMongoRepository extends MongoRepository<UserIdentityDocument, String> {
    List<UserIdentityDocument> findAllByUserId(String userId);
}
