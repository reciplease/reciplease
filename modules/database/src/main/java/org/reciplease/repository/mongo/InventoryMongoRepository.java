package org.reciplease.repository.mongo;

import org.reciplease.model.InventoryItemDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.List;

public interface InventoryMongoRepository extends MongoRepository<InventoryItemDocument, String> {
    List<InventoryItemDocument> findByExpirationGreaterThanEqual(LocalDate date);
    List<InventoryItemDocument> findByExpirationBefore(LocalDate date);
    List<InventoryItemDocument> findByBarcode(String barcode);
    List<InventoryItemDocument> findByNameIgnoreCase(String name);
}
