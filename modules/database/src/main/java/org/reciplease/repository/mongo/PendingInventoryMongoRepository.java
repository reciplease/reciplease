package org.reciplease.repository.mongo;

import org.reciplease.model.PendingInventoryItemDocument;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface PendingInventoryMongoRepository extends MongoRepository<PendingInventoryItemDocument, String> {
    List<PendingInventoryItemDocument> findByHouseId(String houseId, Sort sort);
}
