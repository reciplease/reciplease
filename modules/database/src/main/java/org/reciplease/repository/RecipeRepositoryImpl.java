package org.reciplease.repository;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

import java.util.List;
import java.util.Optional;
import java.util.Set;
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
    public List<Recipe> findVisibleTo(final Set<String> visibleOwnerIds) {
        return mongoTemplate.find(query(visibilityCriteria(visibleOwnerIds)), RecipeDocument.class).stream()
                .map(RecipeDocument::toModel)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Recipe> findVisibleById(final String id, final Set<String> visibleOwnerIds) {
        final var criteria = visibilityCriteria(visibleOwnerIds).and("_id").is(id);
        return Optional.ofNullable(mongoTemplate.findOne(query(criteria), RecipeDocument.class))
                .map(RecipeDocument::toModel);
    }

    private Criteria visibilityCriteria(final Set<String> visibleOwnerIds) {
        final var orConditions = new java.util.ArrayList<Criteria>();
        orConditions.add(where("public").is(true));
        if (visibleOwnerIds != null && !visibleOwnerIds.isEmpty()) {
            orConditions.add(where("ownerId").in(visibleOwnerIds));
        }
        return new Criteria().orOperator(orConditions);
    }
}
