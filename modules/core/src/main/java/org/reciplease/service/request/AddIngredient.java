package org.reciplease.service.request;

import lombok.Value;

import java.util.UUID;

@Value
public class AddIngredient {
    UUID ingredientId;
    Double amount;
}
