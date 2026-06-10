package org.reciplease.repository;

import org.reciplease.model.InventoryItem;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface InventoryRepository {
    Optional<InventoryItem> findById(String id);

    InventoryItem save(InventoryItem item);

    List<InventoryItem> expiresAfter(LocalDate now);

    List<InventoryItem> betweenDates(LocalDate now);

    List<InventoryItem> findByBarcode(String barcode);

    List<InventoryItem> findByBarcodeIn(Collection<String> barcodes);

    List<InventoryItem> findByName(String name);
}
