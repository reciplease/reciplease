package org.reciplease.repository;

import lombok.RequiredArgsConstructor;
import org.reciplease.model.InventoryItem;
import org.reciplease.model.InventoryItemEntity;
import org.reciplease.repository.jpa.InventoryJpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class InventoryRepositoryImpl implements InventoryRepository {
    private final InventoryJpaRepository inventoryJpaRepository;

    @Override
    public Optional<InventoryItem> findById(UUID uuid) {
        return inventoryJpaRepository.findById(uuid).map(InventoryItemEntity::toModel);
    }

    @Override
    public InventoryItem save(InventoryItem item) {
        return inventoryJpaRepository.save(InventoryItemEntity.from(item)).toModel();
    }

    @Override
    public List<InventoryItem> findAll() {
        return inventoryJpaRepository.findAll().stream()
            .map(InventoryItemEntity::toModel)
            .collect(Collectors.toList());
    }

    @Override
    public List<InventoryItem> expiresAfter(LocalDate now) {
        return inventoryJpaRepository.findByExpirationIsGreaterThanEqual(now).stream()
            .map(InventoryItemEntity::toModel)
            .collect(Collectors.toList());
    }

    @Override
    public List<InventoryItem> betweenDates(LocalDate now) {
        return inventoryJpaRepository.findByExpirationIsBefore(now).stream()
            .map(InventoryItemEntity::toModel)
            .collect(Collectors.toList());
    }
}
