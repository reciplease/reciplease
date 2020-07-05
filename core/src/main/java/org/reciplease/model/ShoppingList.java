package org.reciplease.model;

import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
public class ShoppingList {
    private final Set<RecipeItem> items;
}
