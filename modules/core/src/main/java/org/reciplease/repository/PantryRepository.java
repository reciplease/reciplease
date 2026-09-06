package org.reciplease.repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.reciplease.model.PantryItem;

public interface PantryRepository {
    Optional<PantryItem> findById(String id);

    PantryItem save(PantryItem item);

    List<PantryItem> expiresAfter(String houseId, LocalDate now);

    List<PantryItem> betweenDates(String houseId, LocalDate now);

    List<PantryItem> findByBarcode(String houseId, String barcode);

    List<PantryItem> findByBarcodeIn(String houseId, Collection<String> barcodes);

    List<PantryItem> findByName(String houseId, String name);

    List<PantryItem> findAllById(Collection<String> ids);

    void deleteById(String id);
}
