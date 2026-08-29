package org.reciplease.repository.mongo;

import java.util.List;
import java.util.Optional;
import org.reciplease.model.InviteDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface InviteMongoRepository extends MongoRepository<InviteDocument, String> {
    Optional<InviteDocument> findByCode(String code);

    List<InviteDocument> findAllByHouseId(String houseId);
}
