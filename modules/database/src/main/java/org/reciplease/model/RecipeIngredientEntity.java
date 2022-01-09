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

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class RecipeIngredientEntity extends BaseEntity {
    @EmbeddedId
    @JsonIgnore
    @EqualsAndHashCode.Include
    private RecipeIngredientIdEntity id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("recipeUuid")
    @JsonIgnore
    @NonNull
    private RecipeEntity recipe;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("ingredientUuid")
    @NonNull
    private IngredientEntity ingredientEntity;

    @ToString.Include
    @NonNull
    private Double amount;

    public RecipeIngredientEntity(final RecipeEntity recipe, final IngredientEntity ingredientEntity, final Double amount) {
        this.id = new RecipeIngredientIdEntity(recipe.getUuid(), ingredientEntity.getUuid());
        this.ingredientEntity = ingredientEntity;
        this.recipe = recipe;
        this.amount = amount;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        final RecipeIngredientEntity that = (RecipeIngredientEntity) o;

        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    public static RecipeIngredientEntity from(final RecipeIngredient recipeIngredient) {
        return new RecipeIngredientEntity(RecipeEntity.from(recipeIngredient.getRecipe()), recipeIngredient.getIngredient(), recipeIngredient.getAmount());
    }

    public RecipeIngredient toModel() {
        return new RecipeIngredient(this.getRecipe().toModel(), this.getIngredientEntity(), this.getAmount());
    }
}
