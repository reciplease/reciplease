package org.reciplease.service;

import lombok.RequiredArgsConstructor;
import org.reciplease.dto.InventoryItemDto;
import org.reciplease.model.InventoryItem;
import org.reciplease.repository.IngredientRepository;
import org.reciplease.repository.InventoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryService {
    private final InventoryRepository inventoryRepository;
    private final IngredientRepository ingredientRepository;

    public InventoryItemDto save(final InventoryItemDto itemDto) {
        final var ingredient = ingredientRepository.findById(itemDto.getIngredientUuid())
                .orElseThrow(() -> new IllegalArgumentException("Ingredient does not exist"));

        final InventoryItem item = InventoryItem.builder()
                .ingredient(ingredient)
                .expiration(itemDto.getExpiration())
                .amount(itemDto.getAmount())
                .build();

        return InventoryItemDto.from(inventoryRepository.save(item));
    }

    public Optional<InventoryItemDto> findById(final UUID uuid) {
        return inventoryRepository.findById(uuid)
                .map(InventoryItemDto::from);
    }

    public List<InventoryItemDto> findAll() {
        return inventoryRepository.findAll().stream()
                .map(InventoryItemDto::from)
                .collect(Collectors.toList());
    }
}
