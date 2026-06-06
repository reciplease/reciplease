package org.reciplease.repository;

import lombok.RequiredArgsConstructor;
import org.reciplease.model.InventoryItem;
import org.reciplease.model.InventoryItemDocument;
import org.reciplease.repository.mongo.InventoryMongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class InventoryRepositoryImpl implements InventoryRepository {
    private final InventoryMongoRepository inventoryMongoRepository;

    @Override
    public Optional<InventoryItem> findById(final String id) {
        return inventoryMongoRepository.findById(id).map(InventoryItemDocument::toModel);
    }

    @Override
    public InventoryItem save(final InventoryItem item) {
        return inventoryMongoRepository.save(InventoryItemDocument.from(item)).toModel();
    }

    @Override
    public List<InventoryItem> findAll() {
        return inventoryMongoRepository.findAll().stream()
                .map(InventoryItemDocument::toModel)
                .collect(Collectors.toList());
    }

    @Override
    public List<InventoryItem> expiresAfter(final LocalDate date) {
        return inventoryMongoRepository.findByExpirationGreaterThanEqual(date).stream()
                .map(InventoryItemDocument::toModel)
                .collect(Collectors.toList());
    }

    @Override
    public List<InventoryItem> betweenDates(final LocalDate date) {
        return inventoryMongoRepository.findByExpirationBefore(date).stream()
                .map(InventoryItemDocument::toModel)
                .collect(Collectors.toList());
    }

    @Override
    public List<InventoryItem> findByBarcode(final String barcode) {
        return inventoryMongoRepository.findByBarcode(barcode).stream()
                .map(InventoryItemDocument::toModel)
                .collect(Collectors.toList());
    }

    @Override
    public List<InventoryItem> findByName(final String name) {
        return inventoryMongoRepository.findByNameIgnoreCase(name).stream()
                .map(InventoryItemDocument::toModel)
                .collect(Collectors.toList());
    }
}
