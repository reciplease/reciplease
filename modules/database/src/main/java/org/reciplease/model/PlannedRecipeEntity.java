package org.reciplease.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@NoArgsConstructor
@RequiredArgsConstructor
@Getter
public class PlannedRecipeEntity extends BaseEntity {
    @ManyToOne
    @NonNull
    private RecipeEntity recipe;
    @NonNull
    private LocalDate date;

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        final PlannedRecipeEntity that = (PlannedRecipeEntity) o;

        return Objects.equals(getUuid(), that.getUuid());
    }

    @Override
    public int hashCode() {
        return 914579680;
    }

    public PlannedRecipe toModel() {
        return PlannedRecipe.builder()
            .uuid(getUuid())
            .recipe(getRecipe().toModel())
            .date(getDate())
            .build();
    }
}
