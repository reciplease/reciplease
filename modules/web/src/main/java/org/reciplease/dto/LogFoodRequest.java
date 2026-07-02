package org.reciplease.dto;

import java.time.LocalDate;

/** Body of {@code POST /api/fitbit/foods/log}. */
public record LogFoodRequest(String foodId, String unitId, double amount, String mealTypeId, LocalDate date) {}
