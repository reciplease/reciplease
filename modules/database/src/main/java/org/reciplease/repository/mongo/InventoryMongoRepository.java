package org.reciplease.repository.mongo;

import org.reciplease.model.InventoryItemDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface InventoryMongoRepository extends MongoRepository<InventoryItemDocument, UUID> {
    List<InventoryItemDocument> findByExpirationGreaterThanEqual(LocalDate date);
    List<InventoryItemDocument> findByExpirationBefore(LocalDate date);
}
