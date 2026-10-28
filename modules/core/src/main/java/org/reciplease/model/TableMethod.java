package org.reciplease.model;

import java.util.List;

public record TableMethod(List<TableCell> cells) implements RecipeMethod {
    public TableMethod {
        cells = cells == null ? List.of() : List.copyOf(cells);
    }
}
