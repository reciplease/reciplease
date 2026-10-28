package org.reciplease.model;

import java.util.List;

public record TableCell(String text, List<TableCell> inputs) {
    public TableCell {
        inputs = inputs == null ? List.of() : List.copyOf(inputs);
    }

    public static TableCell row(final String text) {
        return new TableCell(text, List.of());
    }
}
