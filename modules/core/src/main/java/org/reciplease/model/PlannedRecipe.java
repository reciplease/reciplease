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
public class PlannedRecipe extends BaseModel {
    @ManyToOne
    @NonNull
    private Recipe recipe;
    @NonNull
    private LocalDate date;

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        final PlannedRecipe that = (PlannedRecipe) o;

        return Objects.equals(getUuid(), that.getUuid());
    }

    @Override
    public int hashCode() {
        return 914579680;
    }
}
