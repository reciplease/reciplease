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
public class RecipeJpa extends BaseEntity {
    private String name;

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL)
    @Builder.Default
    private Set<RecipeIngredientJpa> recipeIngredients = new HashSet<>();

    public RecipeJpa addIngredient(final IngredientJpa ingredientJpa, final Double amount) {
        final var recipeItem = new RecipeIngredientJpa(this, ingredientJpa, amount);
        recipeIngredients.add(recipeItem);
        return this;
    }

    public RecipeJpa removeIngredient(final IngredientJpa ingredientJpa) {
        recipeIngredients = recipeIngredients.stream()
                .filter(not(item -> item.getIngredientJpa().equals(ingredientJpa)))
                .collect(toSet());
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        final RecipeJpa recipe = (RecipeJpa) o;

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

    public static RecipeJpa from(final Recipe recipe) {
        return RecipeJpa.builder()
                .uuid(recipe.getUuid())
                .name(recipe.getName())
                .recipeIngredients(recipe.getRecipeIngredients().stream().map(RecipeIngredientJpa::from).collect(toSet()))
                .build();
    }

    public Recipe toModel() {
        return Recipe.builder()
                .uuid(this.getUuid())
                .name(this.getName())
                .recipeIngredients(this.getRecipeIngredients().stream().map(RecipeIngredientJpa::toModel).collect(toSet()))
                .build();
    }
}
