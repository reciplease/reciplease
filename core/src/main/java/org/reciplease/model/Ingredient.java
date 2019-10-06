package org.reciplease.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Entity
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@Getter
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class Ingredient extends BaseEntity {
    @NotNull
    @NotBlank
    private String name;
    @NotNull
    @Enumerated(value = EnumType.STRING)
    private Measure measure;
}
