package org.reciplease.repository.mongo;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import org.reciplease.model.PantryItemDocument;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PantryMongoRepository extends MongoRepository<PantryItemDocument, String> {
    List<PantryItemDocument> findByHouseIdAndExpirationGreaterThanEqual(String houseId, LocalDate date, Sort sort);

    List<PantryItemDocument> findByHouseIdAndExpirationBefore(String houseId, LocalDate date, Sort sort);

    List<PantryItemDocument> findByHouseIdAndBarcode(String houseId, String barcode);

    List<PantryItemDocument> findByHouseIdAndBarcodeIn(String houseId, Collection<String> barcodes);

    List<PantryItemDocument> findByHouseIdAndNameIgnoreCase(String houseId, String name);

    List<PantryItemDocument> findByRemainingLessThanEqual(Double remaining);
}
