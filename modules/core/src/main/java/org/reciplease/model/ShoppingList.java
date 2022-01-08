package org.reciplease.model;

import lombok.Value;

import java.util.Set;

@Value
public class ShoppingList extends BaseModel {
    Set<RecipeIngredient> items;
}
