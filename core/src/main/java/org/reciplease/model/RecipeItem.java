package org.reciplease.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@EqualsAndHashCode
public class RecipeItem {
    @EmbeddedId
    @JsonIgnore
    private RecipeItemId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("recipeUuid")
    @JsonIgnore
    @NonNull
    private Recipe recipe;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("ingredientUuid")
    @NonNull
    private Ingredient ingredient;

    @NonNull
    private Double amount;

    public RecipeItem(final Recipe recipe, final Ingredient ingredient, final Double amount) {
        this.id = new RecipeItemId(recipe.getUuid(), ingredient.getUuid());
        this.recipe = recipe;
        this.ingredient = ingredient;
        this.amount = amount;
    }
}
