package org.reciplease.repository.mongo;

import java.util.List;
import org.reciplease.model.PendingPantryItemDocument;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PendingPantryMongoRepository extends MongoRepository<PendingPantryItemDocument, String> {
    List<PendingPantryItemDocument> findByHouseId(String houseId, Sort sort);
}
