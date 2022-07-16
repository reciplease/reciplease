package org.reciplease.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotNull;
import java.util.UUID;

@Getter
@EqualsAndHashCode
@SuperBuilder(toBuilder = true)
public abstract class Identifiable {
    @NotNull
    private UUID uuid;
}
