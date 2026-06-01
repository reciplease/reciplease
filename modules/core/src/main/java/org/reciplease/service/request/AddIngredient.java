package org.reciplease.service.request;

import lombok.Value;

@Value
public class AddIngredient {
    String ingredientId;
    Double amount;
}
