package org.reciplease.service;

import lombok.RequiredArgsConstructor;
import org.reciplease.model.InventoryItem;
import org.reciplease.repository.IngredientRepository;
import org.reciplease.repository.InventoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InventoryService {
    private final InventoryRepository inventoryRepository;
    private final IngredientRepository ingredientRepository;

    public InventoryItem save(final InventoryItem item) {
        final var ingredient = ingredientRepository.findById(item.getIngredient().getUuid())
                .orElseThrow(() -> new IllegalArgumentException("Ingredient does not exist"));

        final InventoryItem updatedItem = item.toBuilder()
                .ingredient(ingredient)
                .build();

        return inventoryRepository.save(updatedItem);
    }

    public Optional<InventoryItem> findById(final UUID uuid) {
        return inventoryRepository.findById(uuid);
    }

    public List<InventoryItem> findAll() {
        return inventoryRepository.findAll();
    }
}
