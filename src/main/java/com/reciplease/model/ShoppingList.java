package com.reciplease.model;

import lombok.Data;

import java.util.List;

@Data
public class ShoppingList {
    final List<RecipeItem> items;
}
