package org.reciplease.model;

import java.util.List;
import lombok.Value;

@Value
public class ShoppingList {
    List<RecipeIngredient> items;
}
