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
public class Ingredient extends BaseEntity {
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
        final Ingredient that = (Ingredient) o;

        return Objects.equals(getUuid(), that.getUuid());
    }

    @Override
    public int hashCode() {
        return 1847634289;
    }
}
