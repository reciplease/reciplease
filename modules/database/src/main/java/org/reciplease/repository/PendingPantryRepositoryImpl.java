package org.reciplease.repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.reciplease.model.PendingPantryItem;
import org.reciplease.model.PendingPantryItemDocument;
import org.reciplease.repository.mongo.PendingPantryMongoRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PendingPantryRepositoryImpl implements PendingPantryRepository {
    private final PendingPantryMongoRepository pendingPantryMongoRepository;

    @Override
    public Optional<PendingPantryItem> findById(final String id) {
        return pendingPantryMongoRepository.findById(id).map(PendingPantryItemDocument::toModel);
    }

    @Override
    public PendingPantryItem save(final PendingPantryItem item) {
        if (item.scanId() == null) {
            return persist(item);
        }
        return findByScanId(item).orElseGet(() -> insertOrFindExisting(item));
    }

    PendingPantryItem insertOrFindExisting(final PendingPantryItem item) {
        try {
            return persist(item);
        } catch (final DuplicateKeyException e) {
            return findByScanId(item).orElseThrow(() -> e);
        }
    }

    private PendingPantryItem persist(final PendingPantryItem item) {
        return pendingPantryMongoRepository
                .save(PendingPantryItemDocument.from(item))
                .toModel();
    }

    private Optional<PendingPantryItem> findByScanId(final PendingPantryItem item) {
        return pendingPantryMongoRepository
                .findByHouseIdAndScanId(item.houseId(), item.scanId())
                .map(PendingPantryItemDocument::toModel);
    }

    @Override
    public List<PendingPantryItem> findAllByHouseId(final String houseId) {
        return pendingPantryMongoRepository.findByHouseId(houseId, Sort.by(Sort.Direction.ASC, "createdAt")).stream()
                .map(PendingPantryItemDocument::toModel)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(final String id) {
        pendingPantryMongoRepository.deleteById(id);
    }
}
