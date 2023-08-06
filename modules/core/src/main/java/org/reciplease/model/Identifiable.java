package org.reciplease.model;

import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter
@EqualsAndHashCode
@SuperBuilder(toBuilder = true)
public abstract class Identifiable {

    @NotNull
    private UUID uuid;
}
