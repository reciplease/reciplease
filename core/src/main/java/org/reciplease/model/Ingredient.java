package org.reciplease.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Entity
@NoArgsConstructor
@Getter
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class Ingredient extends BaseEntity {
    @NotNull
    @NotBlank
    private String name;
    @NotNull
    @Enumerated(value = EnumType.STRING)
    private Measure measure;

    @Builder
    private Ingredient(String id, @NotNull @NotBlank final String name, @NotNull final Measure measure) {
        this.id = id;
        this.name = name;
        this.measure = measure;
    }
}
