package org.reciplease.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.hibernate.Hibernate;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Objects;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
@Getter
@ToString
public class IngredientEntity extends BaseEntity {
    @NotNull
    @NotBlank
    private String name;
    @NotNull
    @Enumerated(value = EnumType.STRING)
    private Measure measure;

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        final IngredientEntity that = (IngredientEntity) o;

        return Objects.equals(getUuid(), that.getUuid());
    }

    @Override
    public int hashCode() {
        return 1847634289;
    }

    public static IngredientEntity from(final Ingredient ingredient) {
        return IngredientEntity.builder()
                .uuid(ingredient.getUuid())
                .name(ingredient.getName())
                .measure(ingredient.getMeasure())
                .build();
    }

    public Ingredient toModel() {
        return Ingredient.builder()
                .uuid(getUuid())
                .name(getName())
                .measure(getMeasure())
                .build();
    }
}
