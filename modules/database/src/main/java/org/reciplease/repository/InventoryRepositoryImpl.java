package org.reciplease.repository;

import lombok.RequiredArgsConstructor;
import org.reciplease.model.ArchivedInventoryItemDocument;
import org.reciplease.model.InventoryItem;
import org.reciplease.model.InventoryItemDocument;
import org.reciplease.repository.mongo.InventoryArchiveMongoRepository;
import org.reciplease.repository.mongo.InventoryMongoRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class InventoryRepositoryImpl implements InventoryRepository {
    private final InventoryMongoRepository inventoryMongoRepository;
    private final InventoryArchiveMongoRepository inventoryArchiveMongoRepository;

    @Override
    public Optional<InventoryItem> findById(final String id) {
        return inventoryMongoRepository.findById(id).map(InventoryItemDocument::toModel);
    }

    @Override
    public InventoryItem save(final InventoryItem item) {
        return inventoryMongoRepository.save(InventoryItemDocument.from(item)).toModel();
    }

    @Override
    public List<InventoryItem> expiresAfter(final String houseId, final LocalDate date) {
        return inventoryMongoRepository.findByHouseIdAndExpirationGreaterThanEqual(houseId, date, Sort.by(Sort.Direction.ASC, "name")).stream()
                .map(InventoryItemDocument::toModel)
                .collect(Collectors.toList());
    }

    @Override
    public List<InventoryItem> betweenDates(final String houseId, final LocalDate date) {
        return inventoryMongoRepository.findByHouseIdAndExpirationBefore(houseId, date, Sort.by(Sort.Direction.ASC, "name")).stream()
                .map(InventoryItemDocument::toModel)
                .collect(Collectors.toList());
    }

    @Override
    public List<InventoryItem> findByBarcode(final String houseId, final String barcode) {
        return inventoryMongoRepository.findByHouseIdAndBarcode(houseId, barcode).stream()
                .map(InventoryItemDocument::toModel)
                .collect(Collectors.toList());
    }

    @Override
    public List<InventoryItem> findByBarcodeIn(final String houseId, final Collection<String> barcodes) {
        return inventoryMongoRepository.findByHouseIdAndBarcodeIn(houseId, barcodes).stream()
                .map(InventoryItemDocument::toModel)
                .collect(Collectors.toList());
    }

    @Override
    public List<InventoryItem> findByName(final String houseId, final String name) {
        return inventoryMongoRepository.findByHouseIdAndNameIgnoreCase(houseId, name).stream()
                .map(InventoryItemDocument::toModel)
                .collect(Collectors.toList());
    }

    @Override
    public List<InventoryItem> findAllZeroRemaining() {
        return inventoryMongoRepository.findByRemainingLessThanEqual(0d).stream()
                .map(InventoryItemDocument::toModel)
                .collect(Collectors.toList());
    }

    @Override
    public List<InventoryItem> findAllById(final Collection<String> ids) {
        return inventoryMongoRepository.findAllById(ids).stream()
                .map(InventoryItemDocument::toModel)
                .collect(Collectors.toList());
    }

    // Archives a snapshot of the item into a sibling collection before it's gone for good —
    // a database-only concern, nothing above this layer needs to know it happens.
    @Override
    public void deleteById(final String id) {
        inventoryMongoRepository.findById(id)
                .ifPresent(doc -> inventoryArchiveMongoRepository.save(ArchivedInventoryItemDocument.from(doc, Instant.now())));
        inventoryMongoRepository.deleteById(id);
    }
}
