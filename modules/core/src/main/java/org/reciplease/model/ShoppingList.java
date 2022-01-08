package org.reciplease.model;

import lombok.Value;
import lombok.experimental.SuperBuilder;

import java.util.Set;

@Value
@SuperBuilder(toBuilder = true)
public class ShoppingList extends BaseModel {
    Set<RecipeIngredient> items;
}
