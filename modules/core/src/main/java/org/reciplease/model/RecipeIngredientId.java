package org.reciplease.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.Hibernate;

import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder(toBuilder = true)
public class RecipeIngredientId implements Serializable {
    private UUID recipeUuid;
    private UUID ingredientUuid;

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        final RecipeIngredientId that = (RecipeIngredientId) o;

        if (!Objects.equals(recipeUuid, that.recipeUuid)) return false;
        return Objects.equals(ingredientUuid, that.ingredientUuid);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(recipeUuid);
        result = 31 * result + (Objects.hashCode(ingredientUuid));
        return result;
    }
}
