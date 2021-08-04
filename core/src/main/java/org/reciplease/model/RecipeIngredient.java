package org.reciplease.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class RecipeIngredient {
    @EmbeddedId
    @JsonIgnore
    @EqualsAndHashCode.Include
    private RecipeIngredientId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("recipeUuid")
    @JsonIgnore
    @NonNull
    private Recipe recipe;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("ingredientUuid")
    @ToString.Include
    @NonNull
    private Ingredient ingredient;

    @ToString.Include
    @NonNull
    private Double amount;

    public RecipeIngredient(final Recipe recipe, final Ingredient ingredient, final Double amount) {
        this.id = new RecipeIngredientId(recipe.getUuid(), ingredient.getUuid());
        this.ingredient = ingredient;
        this.recipe = recipe;
        this.amount = amount;
    }

    public RecipeIngredient(final UUID ingredientUuid, final Double amount) {
        this.id = new RecipeIngredientId(null, ingredientUuid);
        this.amount = amount;
    }
}
