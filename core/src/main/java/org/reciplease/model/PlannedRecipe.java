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
public class PlannedRecipe extends BaseEntity {
    @ManyToOne
    @NonNull
    private Recipe recipe;
    @NonNull
    private LocalDate date;
}
