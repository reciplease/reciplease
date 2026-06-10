package org.reciplease.repository.mongo;

import org.reciplease.model.InventoryItemDocument;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface InventoryMongoRepository extends MongoRepository<InventoryItemDocument, String> {
    List<InventoryItemDocument> findByExpirationGreaterThanEqual(LocalDate date, Sort sort);
    List<InventoryItemDocument> findByExpirationBefore(LocalDate date, Sort sort);
    List<InventoryItemDocument> findByBarcode(String barcode);
    List<InventoryItemDocument> findByBarcodeIn(Collection<String> barcodes);
    List<InventoryItemDocument> findByNameIgnoreCase(String name);
}
