package org.reciplease.model;

import java.util.List;

public record TableCell(String text, List<TableCell> inputs, CellDuration duration, boolean handsOff) {
    public TableCell {
        inputs = inputs == null ? List.of() : List.copyOf(inputs);
    }

    public TableCell(final String text, final List<TableCell> inputs) {
        this(text, inputs, null, false);
    }

    public static TableCell row(final String text) {
        return new TableCell(text, List.of());
    }
}
