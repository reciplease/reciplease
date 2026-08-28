package org.reciplease.service;

import lombok.RequiredArgsConstructor;
import org.reciplease.model.PantryItem;
import org.reciplease.model.PendingPantryItem;
import org.reciplease.repository.PantryRepository;
import org.reciplease.repository.PendingPantryRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PendingPantryService {
    private final PendingPantryRepository pendingPantryRepository;
    private final PantryRepository pantryRepository;

    public PendingPantryItem save(final PendingPantryItem item) {
        return pendingPantryRepository.save(item);
    }

    public Optional<PendingPantryItem> findById(final String id) {
        return pendingPantryRepository.findById(id);
    }

    /**
     * Lists a house's pending items, self-healing along the way: a pending item whose id already
     * exists in the pantry is a completion whose final delete step failed (see
     * {@link #complete}), so it's silently deleted and excluded rather than shown for
     * re-processing.
     */
    public List<PendingPantryItem> findAll(final String houseId) {
        return pendingPantryRepository.findAllByHouseId(houseId).stream()
                .filter(pending -> {
                    if (pantryRepository.findById(pending.id()).isEmpty()) {
                        return true;
                    }
                    pendingPantryRepository.deleteById(pending.id());
                    return false;
                })
                .toList();
    }

    public void deleteById(final String id) {
        pendingPantryRepository.deleteById(id);
    }

    /**
     * Digitises a pending capture: saves the completed pantry item, then deletes the pending
     * one. There are no cross-collection transactions here, so instead the pantry item reuses
     * the pending item's id — re-completing after a failure overwrites the same document rather
     * than duplicating it, and {@link #findAll} sweeps up pending items whose save succeeded but
     * whose delete didn't.
     */
    public PantryItem complete(final String pendingId, final PantryItem item) {
        final var pending = pendingPantryRepository.findById(pendingId)
                .orElseThrow(() -> new IllegalArgumentException("Pending pantry item does not exist"));

        // Re-saving under an id that already exists in the pantry is the retry path (a
        // previous complete whose delete step failed) — but only within the same house. An id
        // pointing at another house's item must never be overwritten.
        pantryRepository.findById(pendingId)
                .filter(existing -> !existing.houseId().equals(item.houseId()))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Pending pantry item id collides with another house's pantry item");
                });

        // Mongo auditing only populates @CreatedBy/@CreatedDate on inserts, and reusing the
        // pending id makes this save an upsert — so creation metadata is carried over from the
        // pending capture explicitly (the capture is when this item came into existence).
        final var merged = new PantryItem(pendingId, pending.createdBy(), item.houseId(), item.name(), item.brand(), item.measure(),
                item.amount(), item.remaining(), item.expiration(), item.barcode(), item.image(),
                pending.createdAt(), null);

        final var saved = pantryRepository.save(merged);
        pendingPantryRepository.deleteById(pendingId);
        return saved;
    }
}
