package org.reciplease.repository;

import lombok.RequiredArgsConstructor;
import org.reciplease.model.PendingInventoryItem;
import org.reciplease.model.PendingInventoryItemDocument;
import org.reciplease.repository.mongo.PendingInventoryMongoRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class PendingInventoryRepositoryImpl implements PendingInventoryRepository {
    private final PendingInventoryMongoRepository pendingInventoryMongoRepository;

    @Override
    public Optional<PendingInventoryItem> findById(final String id) {
        return pendingInventoryMongoRepository.findById(id).map(PendingInventoryItemDocument::toModel);
    }

    @Override
    public PendingInventoryItem save(final PendingInventoryItem item) {
        return pendingInventoryMongoRepository.save(PendingInventoryItemDocument.from(item)).toModel();
    }

    @Override
    public List<PendingInventoryItem> findAllByHouseId(final String houseId) {
        return pendingInventoryMongoRepository.findByHouseId(houseId, Sort.by(Sort.Direction.ASC, "createdAt")).stream()
                .map(PendingInventoryItemDocument::toModel)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(final String id) {
        pendingInventoryMongoRepository.deleteById(id);
    }
}
