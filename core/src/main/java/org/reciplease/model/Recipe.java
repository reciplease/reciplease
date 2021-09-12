package org.reciplease.model;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
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
@SuperBuilder(toBuilder = true)
@Getter
@ToString
public class Recipe extends BaseEntity {
    private String name;

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL)
    @Builder.Default
    private Set<RecipeIngredient> recipeIngredients = new HashSet<>();

    public Recipe addIngredient(final Ingredient ingredient, final Double amount) {
        final var recipeItem = new RecipeIngredient(this, ingredient, amount);
        recipeIngredients.add(recipeItem);
        return this;
    }

    public Recipe removeIngredient(final Ingredient ingredient) {
        recipeIngredients = recipeIngredients.stream()
                .filter(not(item -> item.getIngredient().equals(ingredient)))
                .collect(toSet());
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        final Recipe recipe = (Recipe) o;

        return Objects.equals(getUuid(), recipe.getUuid());
    }

    @Override
    public int hashCode() {
        return 1629938687;
    }
}
