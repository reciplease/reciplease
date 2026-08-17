package org.reciplease.service;

import lombok.RequiredArgsConstructor;
import org.reciplease.model.InventoryItem;
import org.reciplease.model.PendingInventoryItem;
import org.reciplease.repository.InventoryRepository;
import org.reciplease.repository.PendingInventoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PendingInventoryService {
    private final PendingInventoryRepository pendingInventoryRepository;
    private final InventoryRepository inventoryRepository;

    public PendingInventoryItem save(final PendingInventoryItem item) {
        return pendingInventoryRepository.save(item);
    }

    public Optional<PendingInventoryItem> findById(final String id) {
        return pendingInventoryRepository.findById(id);
    }

    /**
     * Lists a house's pending items, self-healing along the way: a pending item whose id already
     * exists in the inventory is a completion whose final delete step failed (see
     * {@link #complete}), so it's silently deleted and excluded rather than shown for
     * re-processing.
     */
    public List<PendingInventoryItem> findAll(final String houseId) {
        return pendingInventoryRepository.findAllByHouseId(houseId).stream()
                .filter(pending -> {
                    if (inventoryRepository.findById(pending.id()).isEmpty()) {
                        return true;
                    }
                    pendingInventoryRepository.deleteById(pending.id());
                    return false;
                })
                .toList();
    }

    public void deleteById(final String id) {
        pendingInventoryRepository.deleteById(id);
    }

    /**
     * Digitises a pending capture: saves the completed inventory item, then deletes the pending
     * one. There are no cross-collection transactions here, so instead the inventory item reuses
     * the pending item's id — re-completing after a failure overwrites the same document rather
     * than duplicating it, and {@link #findAll} sweeps up pending items whose save succeeded but
     * whose delete didn't.
     */
    public InventoryItem complete(final String pendingId, final InventoryItem item) {
        final var pending = pendingInventoryRepository.findById(pendingId)
                .orElseThrow(() -> new IllegalArgumentException("Pending inventory item does not exist"));

        // Re-saving under an id that already exists in the inventory is the retry path (a
        // previous complete whose delete step failed) — but only within the same house. An id
        // pointing at another house's item must never be overwritten.
        inventoryRepository.findById(pendingId)
                .filter(existing -> !existing.houseId().equals(item.houseId()))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Pending inventory item id collides with another house's inventory item");
                });

        // Mongo auditing only populates @CreatedBy/@CreatedDate on inserts, and reusing the
        // pending id makes this save an upsert — so creation metadata is carried over from the
        // pending capture explicitly (the capture is when this item came into existence).
        final var merged = new InventoryItem(pendingId, pending.createdBy(), item.houseId(), item.name(), item.brand(), item.measure(),
                item.amount(), item.remaining(), item.expiration(), item.barcode(), item.image(),
                pending.createdAt(), null);

        final var saved = inventoryRepository.save(merged);
        pendingInventoryRepository.deleteById(pendingId);
        return saved;
    }
}
