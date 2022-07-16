package org.reciplease.repository.jpa;

import org.reciplease.model.InventoryItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

public interface InventoryJpaRepository extends JpaRepository<InventoryItemEntity, UUID> {
    Set<InventoryItemEntity> findByExpirationIsGreaterThanEqual(LocalDate date);

    Set<InventoryItemEntity> findByExpirationIsBefore(LocalDate date);
}
