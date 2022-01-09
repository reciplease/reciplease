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

import static java.util.function.Predicate.not;
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
    private Set<RecipeIngredientEntity> recipeIngredients = new HashSet<>();

    public RecipeEntity addIngredient(final IngredientEntity ingredientEntity, final Double amount) {
        final var recipeItem = new RecipeIngredientEntity(this, ingredientEntity, amount);
        recipeIngredients.add(recipeItem);
        return this;
    }

    public RecipeEntity removeIngredient(final IngredientEntity ingredientEntity) {
        recipeIngredients = recipeIngredients.stream()
                .filter(not(item -> item.getIngredientEntity().equals(ingredientEntity)))
                .collect(toSet());
        return this;
    }

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
        return RecipeEntity.builder()
                .uuid(recipe.getUuid())
                .name(recipe.getName())
                .recipeIngredients(recipe.getRecipeIngredients().stream().map(RecipeIngredientEntity::from).collect(toSet()))
                .build();
    }

    public Recipe toModel() {
        return Recipe.builder()
                .uuid(getUuid())
                .name(getName())
                .recipeIngredients(getRecipeIngredients().stream().map(RecipeIngredientEntity::toModel).collect(toSet()))
                .build();
    }
}
