package org.reciplease.repository.mongo;

import org.reciplease.model.InventoryItemDocument;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface InventoryMongoRepository extends MongoRepository<InventoryItemDocument, String> {
    List<InventoryItemDocument> findByHouseIdAndExpirationGreaterThanEqual(String houseId, LocalDate date, Sort sort);
    List<InventoryItemDocument> findByHouseIdAndExpirationBefore(String houseId, LocalDate date, Sort sort);
    List<InventoryItemDocument> findByHouseIdAndBarcode(String houseId, String barcode);
    List<InventoryItemDocument> findByHouseIdAndBarcodeIn(String houseId, Collection<String> barcodes);
    List<InventoryItemDocument> findByHouseIdAndNameIgnoreCase(String houseId, String name);
    List<InventoryItemDocument> findByRemainingLessThanEqual(Double remaining);
}
