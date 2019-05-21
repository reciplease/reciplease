package com.reciplease.model;

import lombok.Data;

import java.util.List;

@Data
public class ShoppingList {
    private final List<RecipeItem> items;
}
