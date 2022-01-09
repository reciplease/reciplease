package org.reciplease.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.Hibernate;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
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

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL)
    @Builder.Default
    private Set<RecipeIngredientEntity> recipeIngredientEntities = new HashSet<>();

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        final RecipeEntity recipe = (RecipeEntity) o;

        return Objects.equals(getUuid(), recipe.getUuid());
    }

    @Override
    public int hashCode() {
        return 1629938687;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" +
                "uuid = " + getUuid() + ", " +
                "name = " + getName() + ")";
    }

    public static RecipeEntity from(final Recipe recipe) {
        final var recipeIngredientEntities = recipe.getRecipeIngredients().stream()
            .map(RecipeIngredientEntity::from)
            .collect(toSet());
        return RecipeEntity.builder()
                .uuid(recipe.getUuid())
                .name(recipe.getName())
                .recipeIngredientEntities(recipeIngredientEntities)
                .build();
    }

    public Recipe toModel() {
        final var recipeIngredients = getRecipeIngredientEntities().stream()
            .map(RecipeIngredientEntity::toModel)
            .collect(toSet());
        return Recipe.builder()
                .uuid(getUuid())
                .name(getName())
                .recipeIngredients(recipeIngredients)
                .build();
    }
}
