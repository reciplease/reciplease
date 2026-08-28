package org.reciplease.repository.mongo;

import org.reciplease.model.PendingPantryItemDocument;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface PendingPantryMongoRepository extends MongoRepository<PendingPantryItemDocument, String> {
    List<PendingPantryItemDocument> findByHouseId(String houseId, Sort sort);
}
