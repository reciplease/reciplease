package org.reciplease.service;

import lombok.RequiredArgsConstructor;
import org.reciplease.model.InventoryItem;
import org.reciplease.repository.InventoryRepository;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InventoryService {
    private final InventoryRepository inventoryRepository;
    private final Clock clock;

    public InventoryItem save(final InventoryItem item) {
        return inventoryRepository.save(item);
    }

    // Merges editable fields from `updates` onto the existing item, preserving its id,
    // createdBy, houseId and createdAt so an edit (e.g. attaching a photo after the fact)
    // can't clobber audit/ownership fields the way a plain re-save would. Callers that only
    // mean to change other fields must resend the item's current `remaining` (InventoryItem's
    // constructor otherwise defaults a missing one to `amount`, which would wipe out how much
    // has been used).
    public InventoryItem update(final String id, final InventoryItem updates) {
        final var existing = inventoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Inventory item does not exist"));

        final var merged = new InventoryItem(existing.id(), existing.createdBy(), existing.houseId(), updates.name(), updates.measure(),
                updates.amount(), updates.remaining(), updates.expiration(), updates.barcode(), updates.image(),
                existing.createdAt(), existing.updatedAt());

        return inventoryRepository.save(merged);
    }

    public Optional<InventoryItem> findById(final String id) {
        return inventoryRepository.findById(id);
    }

    public List<InventoryItem> findAll(final String houseId) {
        final var today = LocalDate.now(clock);
        final var items = new ArrayList<InventoryItem>(inventoryRepository.expiresAfter(houseId, today));
        items.addAll(inventoryRepository.betweenDates(houseId, today));
        return items;
    }

    public List<InventoryItem> findAllUnexpired(final String houseId) {
        return inventoryRepository.expiresAfter(houseId, LocalDate.now(clock));
    }

    public List<InventoryItem> findAllExpired(final String houseId) {
        return inventoryRepository.betweenDates(houseId, LocalDate.now(clock));
    }

    public void deleteById(final String id) {
        inventoryRepository.deleteById(id);
    }
}
