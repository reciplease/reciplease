package org.reciplease.repository;

import static org.springframework.data.mongodb.core.query.Query.query;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.reciplease.model.Recipe;
import org.reciplease.model.RecipeDocument;
import org.reciplease.repository.mongo.RecipeMongoRepository;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RecipeRepositoryImpl implements RecipeRepository {
    private final RecipeMongoRepository recipeMongoRepository;
    private final MongoTemplate mongoTemplate;

    @Override
    public List<Recipe> findAll() {
        return recipeMongoRepository.findAll().stream()
                .map(RecipeDocument::toModel)
                .collect(Collectors.toList());
    }

    @Override
    public Recipe save(final Recipe recipe) {
        return recipeMongoRepository.save(RecipeDocument.from(recipe)).toModel();
    }

    @Override
    public Optional<Recipe> findById(final String id) {
        return recipeMongoRepository.findById(id).map(RecipeDocument::toModel);
    }

    @Override
    public void deleteById(final String id) {
        recipeMongoRepository.deleteById(id);
    }

    @Override
    public List<Recipe> findVisibleTo(final String houseId) {
        return mongoTemplate.find(query(visibilityCriteria(houseId)), RecipeDocument.class).stream()
                .map(RecipeDocument::toModel)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Recipe> findVisibleById(final String id, final String houseId) {
        final var criteria = visibilityCriteria(houseId).and("_id").is(id);
        return Optional.ofNullable(mongoTemplate.findOne(query(criteria), RecipeDocument.class))
                .map(RecipeDocument::toModel);
    }

    private Criteria visibilityCriteria(final String houseId) {
        return houseId == null
                ? Criteria.where("public").is(true)
                : new Criteria()
                        .orOperator(
                                Criteria.where("public").is(true),
                                Criteria.where("houseId").is(houseId));
    }
}
