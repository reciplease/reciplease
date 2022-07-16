package org.reciplease.service;

import lombok.RequiredArgsConstructor;
import org.reciplease.model.InventoryItem;
import org.reciplease.repository.IngredientRepository;
import org.reciplease.repository.InventoryRepository;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InventoryService {
    private final InventoryRepository inventoryRepository;
    private final IngredientRepository ingredientRepository;
    private final Clock clock;

    public InventoryItem save(final InventoryItem item) {
        final var ingredient = ingredientRepository.findByUuid(item.getIngredient().getUuid())
                .orElseThrow(() -> new IllegalArgumentException("Ingredient does not exist"));

        final InventoryItem updatedItem = item.toBuilder()
                .ingredient(ingredient)
                .build();

        return inventoryRepository.save(updatedItem);
    }

    public Optional<InventoryItem> findById(final UUID uuid) {
        return inventoryRepository.findByUuid(uuid);
    }

    public List<InventoryItem> findAll() {
        return inventoryRepository.findAll();
    }

    public List<InventoryItem> findAllUnexpired() {
        return inventoryRepository.expiresAfter(LocalDate.now(clock));
    }

    public List<InventoryItem> findAllExpired() {
        return inventoryRepository.betweenDates(LocalDate.now(clock));
    }
}
