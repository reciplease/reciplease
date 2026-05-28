package org.reciplease.repository.mongo;

import org.reciplease.model.PlannedRecipeDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface PlannedRecipeMongoRepository extends MongoRepository<PlannedRecipeDocument, UUID> {
    List<PlannedRecipeDocument> findByDateBetween(LocalDate start, LocalDate end);
}
