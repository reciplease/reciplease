package org.reciplease.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import org.hibernate.Hibernate;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import java.util.Objects;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class RecipeIngredientJpa {
    @EmbeddedId
    @JsonIgnore
    @EqualsAndHashCode.Include
    private RecipeIngredientId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("recipeUuid")
    @JsonIgnore
    @NonNull
    private RecipeJpa recipe;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("ingredientUuid")
    @NonNull
    private Ingredient ingredient;

    @ToString.Include
    @NonNull
    private Double amount;

    public RecipeIngredientJpa(final RecipeJpa recipe, final Ingredient ingredient, final Double amount) {
        this.id = new RecipeIngredientId(recipe.getUuid(), ingredient.getUuid());
        this.ingredient = ingredient;
        this.recipe = recipe;
        this.amount = amount;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        final RecipeIngredientJpa that = (RecipeIngredientJpa) o;

        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    public static RecipeIngredientJpa from(final RecipeIngredient recipeIngredient) {
        return new RecipeIngredientJpa(RecipeJpa.from(recipeIngredient.getRecipe()), recipeIngredient.getIngredient(), recipeIngredient.getAmount());
    }

    public RecipeIngredient toModel() {
        return new RecipeIngredient(this.getRecipe().toModel(), this.getIngredient(), this.getAmount());
    }
}
