package org.reciplease.model;

import lombok.AllArgsConstructor;
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
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
@Getter
public class InventoryItemEntity extends BaseEntity {
    @ManyToOne
    @NonNull
    private IngredientEntity ingredientEntity;
    @NonNull
    private Double amount;
    @NonNull
    private LocalDate expiration;

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        final InventoryItemEntity that = (InventoryItemEntity) o;

        return Objects.equals(getUuid(), that.getUuid());
    }

    @Override
    public int hashCode() {
        return 20328338;
    }

    public static InventoryItemEntity from(final InventoryItem inventoryItem) {
        return InventoryItemEntity.builder()
                .uuid(inventoryItem.getUuid())
                .ingredientEntity(IngredientEntity.from(inventoryItem.getIngredient()))
                .amount(inventoryItem.getAmount())
                .expiration(inventoryItem.getExpiration())
                .build();
    }

    public InventoryItem toModel() {
        return InventoryItem.builder()
                .uuid(getUuid())
                .ingredient(getIngredientEntity().toModel())
                .amount(getAmount())
                .expiration(getExpiration())
                .build();
    }
}
