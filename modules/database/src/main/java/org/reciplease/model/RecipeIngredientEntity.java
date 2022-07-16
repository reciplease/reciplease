package org.reciplease.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
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
@SuperBuilder(toBuilder = true)
@Getter
public class RecipeIngredientEntity {
    @EmbeddedId
    @JsonIgnore
    @EqualsAndHashCode.Include
    private RecipeIngredientIdEntity id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("recipeUuid")
    @JsonIgnore
    @NonNull
    private RecipeEntity recipeEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("ingredientUuid")
    @NonNull
    private IngredientEntity ingredientEntity;

    @ToString.Include
    @NonNull
    private Double amount;

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

    public static RecipeIngredientEntity from(final RecipeEntity recipeEntity, final RecipeIngredient recipeIngredient) {
        final var ingredientEntity = IngredientEntity.from(recipeIngredient.getIngredient());
        final var recipeIngredientId = new RecipeIngredientIdEntity(recipeEntity.getUuid(), ingredientEntity.getUuid());
        return RecipeIngredientEntity.builder()
                .id(recipeIngredientId)
                .recipeEntity(recipeEntity)
                .ingredientEntity(ingredientEntity)
                .amount(recipeIngredient.getAmount())
                .build();
    }

    public RecipeIngredient toModel() {
        return new RecipeIngredient(getIngredientEntity().toModel(), getAmount());
    }
}
