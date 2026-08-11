package org.reciplease.repository;

import org.reciplease.model.InventoryItem;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface InventoryRepository {
    Optional<InventoryItem> findById(String id);

    InventoryItem save(InventoryItem item);

    List<InventoryItem> expiresAfter(String houseId, LocalDate now);

    List<InventoryItem> betweenDates(String houseId, LocalDate now);

    List<InventoryItem> findByBarcode(String houseId, String barcode);

    List<InventoryItem> findByBarcodeIn(String houseId, Collection<String> barcodes);

    List<InventoryItem> findByName(String houseId, String name);

    List<InventoryItem> findAllZeroRemaining();

    List<InventoryItem> findAllById(Collection<String> ids);

    void deleteById(String id);
}
