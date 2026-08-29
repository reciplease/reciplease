package org.reciplease.repository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.reciplease.model.ArchivedPantryItemDocument;
import org.reciplease.model.PantryItem;
import org.reciplease.model.PantryItemDocument;
import org.reciplease.repository.mongo.PantryArchiveMongoRepository;
import org.reciplease.repository.mongo.PantryMongoRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PantryRepositoryImpl implements PantryRepository {
    private final PantryMongoRepository pantryMongoRepository;
    private final PantryArchiveMongoRepository pantryArchiveMongoRepository;

    @Override
    public Optional<PantryItem> findById(final String id) {
        return pantryMongoRepository.findById(id).map(PantryItemDocument::toModel);
    }

    @Override
    public PantryItem save(final PantryItem item) {
        return pantryMongoRepository.save(PantryItemDocument.from(item)).toModel();
    }

    @Override
    public List<PantryItem> expiresAfter(final String houseId, final LocalDate date) {
        return pantryMongoRepository
                .findByHouseIdAndExpirationGreaterThanEqual(houseId, date, Sort.by(Sort.Direction.ASC, "name"))
                .stream()
                .map(PantryItemDocument::toModel)
                .collect(Collectors.toList());
    }

    @Override
    public List<PantryItem> betweenDates(final String houseId, final LocalDate date) {
        return pantryMongoRepository
                .findByHouseIdAndExpirationBefore(houseId, date, Sort.by(Sort.Direction.ASC, "name"))
                .stream()
                .map(PantryItemDocument::toModel)
                .collect(Collectors.toList());
    }

    @Override
    public List<PantryItem> findByBarcode(final String houseId, final String barcode) {
        return pantryMongoRepository.findByHouseIdAndBarcode(houseId, barcode).stream()
                .map(PantryItemDocument::toModel)
                .collect(Collectors.toList());
    }

    @Override
    public List<PantryItem> findByBarcodeIn(final String houseId, final Collection<String> barcodes) {
        return pantryMongoRepository.findByHouseIdAndBarcodeIn(houseId, barcodes).stream()
                .map(PantryItemDocument::toModel)
                .collect(Collectors.toList());
    }

    @Override
    public List<PantryItem> findByName(final String houseId, final String name) {
        return pantryMongoRepository.findByHouseIdAndNameIgnoreCase(houseId, name).stream()
                .map(PantryItemDocument::toModel)
                .collect(Collectors.toList());
    }

    @Override
    public List<PantryItem> findAllZeroRemaining() {
        return pantryMongoRepository.findByRemainingLessThanEqual(0d).stream()
                .map(PantryItemDocument::toModel)
                .collect(Collectors.toList());
    }

    @Override
    public List<PantryItem> findAllById(final Collection<String> ids) {
        return pantryMongoRepository.findAllById(ids).stream()
                .map(PantryItemDocument::toModel)
                .collect(Collectors.toList());
    }

    // Archives a snapshot of the item into a sibling collection before it's gone for good —
    // a database-only concern, nothing above this layer needs to know it happens.
    @Override
    public void deleteById(final String id) {
        pantryMongoRepository
                .findById(id)
                .ifPresent(
                        doc -> pantryArchiveMongoRepository.save(ArchivedPantryItemDocument.from(doc, Instant.now())));
        pantryMongoRepository.deleteById(id);
    }
}
