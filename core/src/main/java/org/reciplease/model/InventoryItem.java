package org.reciplease.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.time.LocalDate;

@Entity
@NoArgsConstructor
@RequiredArgsConstructor
@Getter
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class InventoryItem extends BaseEntity {
    @ManyToOne
    @NonNull
    private Ingredient ingredient;
    @NonNull
    private Double amount;
    @NonNull
    private LocalDate expiration;
}
