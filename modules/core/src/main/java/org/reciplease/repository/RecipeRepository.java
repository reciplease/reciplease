package org.reciplease.repository;

import java.util.List;
import java.util.Optional;
import org.reciplease.model.Recipe;

public interface RecipeRepository {
    List<Recipe> findAll();

    Recipe save(Recipe recipe);

    Optional<Recipe> findById(String id);

    void deleteById(String id);

    /**
     * Public recipes plus, if {@code houseId} is non-null, private recipes belonging to
     * that house. {@code houseId} is null for unauthenticated callers or callers with no
     * active house, in which case only public recipes are returned.
     */
    List<Recipe> findVisibleTo(String houseId);

    Optional<Recipe> findVisibleById(String id, String houseId);
}
