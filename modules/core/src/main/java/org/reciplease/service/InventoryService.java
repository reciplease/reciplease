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

import static java.util.stream.Collectors.toList;

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
    public Optional<InventoryItem> update(final String id, final InventoryItem updates) {
        final var existing = inventoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Inventory item does not exist"));

        final var merged = new InventoryItem(existing.id(), existing.createdBy(), existing.houseId(), updates.name(), updates.brand(), updates.measure(),
                updates.amount(), updates.remaining(), updates.expiration(), updates.barcode(), updates.image(),
                existing.createdAt(), existing.updatedAt());

        return saveOrArchive(merged);
    }

    public Optional<InventoryItem> findById(final String id) {
        return inventoryRepository.findById(id);
    }

    public List<InventoryItem> findAll(final String houseId) {
        return findAll(houseId, false);
    }

    /**
     * {@code excludeFullyConsumed} drops items with nothing left ({@code remaining <= 0}) —
     * used by the "expiring soon" view, where a fully-eaten/binned item has nothing left to
     * expire and would otherwise show up as if it still needed attention. The pantry list
     * keeps calling the single-arg overload: it deliberately keeps those items visible
     * (greyed out) in case the user wants to restock.
     */
    public List<InventoryItem> findAll(final String houseId, final boolean excludeFullyConsumed) {
        final var today = LocalDate.now(clock);
        final var items = new ArrayList<InventoryItem>(inventoryRepository.expiresAfter(houseId, today));
        items.addAll(inventoryRepository.betweenDates(houseId, today));
        if (!excludeFullyConsumed) {
            return items;
        }
        return items.stream().filter(item -> item.remaining() > 0).collect(toList());
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

    /**
     * Reduces {@code remaining} by {@code amount}, clamped at zero rather than going negative.
     * Empty return means the item was fully consumed and has been deleted (see {@link
     * #saveOrArchive}) rather than left behind as a live zero-remaining record.
     */
    public Optional<InventoryItem> consume(final String id, final Double amount) {
        final var existing = inventoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Inventory item does not exist"));

        return saveOrArchive(existing.withRemaining(Math.max(0, existing.remaining() - amount)));
    }

    /** One-off cleanup for items already sitting at zero remaining from before this behavior existed. */
    public void archiveAllZeroRemainingItems() {
        inventoryRepository.findAllZeroRemaining().forEach(item -> inventoryRepository.deleteById(item.id()));
    }

    /**
     * An item with nothing left has no reason to stay in the live pantry list — deleting it
     * (which archives a snapshot at the repository layer) keeps that list from accumulating
     * zeroed-out rows forever.
     */
    private Optional<InventoryItem> saveOrArchive(final InventoryItem item) {
        if (item.remaining() <= 0) {
            inventoryRepository.deleteById(item.id());
            return Optional.empty();
        }
        return Optional.of(inventoryRepository.save(item));
    }
}
