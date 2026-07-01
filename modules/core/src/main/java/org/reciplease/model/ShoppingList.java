package org.reciplease.model;

import lombok.Value;

import java.util.List;

@Value
public class ShoppingList {
    List<RecipeIngredient> items;
}
