package org.reciplease.model;

import lombok.Value;

import java.util.Set;

@Value
public class ShoppingList {
    Set<RecipeIngredient> items;
}
