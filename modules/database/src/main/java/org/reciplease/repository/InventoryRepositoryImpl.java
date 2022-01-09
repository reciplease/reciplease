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

@Repository
@RequiredArgsConstructor
public class InventoryRepositoryImpl implements InventoryRepository {
    private final InventoryJpaRepository inventoryJpaRepository;

    @Override
    public Optional<InventoryItem> findById(UUID uuid) {
        return Optional.empty();
    }

    @Override
    public InventoryItem save(InventoryItem item) {
        return inventoryJpaRepository.save(InventoryItemEntity.from(item)).toModel();
    }

    @Override
    public List<InventoryItem> findAll() {
        return null;
    }

    @Override
    public List<InventoryItem> findByExpirationIsGreaterThanEqual(LocalDate now) {
        return null;
    }

    @Override
    public List<InventoryItem> findByExpirationIsBefore(LocalDate now) {
        return inventoryJpaRepository.findB;
    }
}
