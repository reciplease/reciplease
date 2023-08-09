package org.reciplease.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.Hibernate;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
@Getter
public class RecipeEntity extends BaseEntity {
    
    private String name;

    @OneToMany(mappedBy = "recipeEntity", cascade = CascadeType.ALL)
    @Builder.Default
    private Set<RecipeIngredientEntity> recipeIngredientEntities = new HashSet<>();

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        final RecipeEntity recipe = (RecipeEntity) o;

        return Objects.equals(getId(), recipe.getId());
    }

    @Override
    public int hashCode() {
        return 1629938687;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" +
                "id = " + getId() + ", " +
                "name = " + getName() + ")";
    }

    public static RecipeEntity from(final Recipe recipe) {
        final RecipeEntity recipeEntity = RecipeEntity.builder()
                .id(recipe.getId())
                .name(recipe.getName())
                .build();
        final var recipeIngredientEntities = recipe.getRecipeIngredients().stream()
                .map(recipeIngredient -> RecipeIngredientEntity.from(recipeEntity, recipeIngredient))
                .collect(toSet());
        recipeEntity.toBuilder()
                .recipeIngredientEntities(recipeIngredientEntities)
                .build();
        return recipeEntity;
    }

    public Recipe toModel() {
        Recipe recipe = Recipe.builder()
                .id(getId())
                .name(getName())
                .build();
        getRecipeIngredientEntities().stream()
                .map(RecipeIngredientEntity::toModel)
                .forEach(recipe::addIngredient);
        return recipe;
    }
}
