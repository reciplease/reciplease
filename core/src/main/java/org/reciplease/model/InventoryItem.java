package org.reciplease.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import org.hibernate.Hibernate;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@Getter
public class InventoryItem extends BaseEntity {
    @ManyToOne
    @NonNull
    private Ingredient ingredient;
    @NonNull
    private Double amount;
    @NonNull
    private LocalDate expiration;

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        final InventoryItem that = (InventoryItem) o;

        return Objects.equals(getUuid(), that.getUuid());
    }

    @Override
    public int hashCode() {
        return 20328338;
    }
}
