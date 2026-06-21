package org.reciplease.repository.mongo;

import org.reciplease.model.InviteDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface InviteMongoRepository extends MongoRepository<InviteDocument, String> {
    Optional<InviteDocument> findByCode(String code);
}
