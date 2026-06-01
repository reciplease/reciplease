package org.reciplease.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@EqualsAndHashCode
@SuperBuilder(toBuilder = true)
public abstract class Identifiable {
    private String id;
}
