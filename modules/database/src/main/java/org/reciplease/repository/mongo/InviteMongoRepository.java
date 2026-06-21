package org.reciplease.repository.mongo;

import org.reciplease.model.InviteDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface InviteMongoRepository extends MongoRepository<InviteDocument, String> {
    Optional<InviteDocument> findByCode(String code);

    List<InviteDocument> findAllByHouseId(String houseId);
}
