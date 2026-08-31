package org.reciplease.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.reciplease.model.Recipe;

public interface RecipeRepository {
    List<Recipe> findAll();

    Recipe save(Recipe recipe);

    Optional<Recipe> findById(String id);

    void deleteById(String id);

    /**
     * Recipes visible to a viewer: every public recipe plus any recipe owned by a user in
     * {@code visibleOwnerIds}. For anonymous callers {@code visibleOwnerIds} is empty and only
     * public recipes are returned. The caller resolves {@code visibleOwnerIds} to the viewer
     * plus everyone who shares a house with the viewer, so a recipe's "members-only" visibility
     * is evaluated against live house membership.
     */
    List<Recipe> findVisibleTo(Set<String> visibleOwnerIds);

    Optional<Recipe> findVisibleById(String id, Set<String> visibleOwnerIds);
}
