package org.reciplease.dto;

import java.time.LocalDate;

/** Body of {@code POST /api/google-health/foods/log}. */
public record LogGoogleHealthFoodRequest(String foodId, String foodDisplayName, String mealType, double amount, LocalDate date) {}
