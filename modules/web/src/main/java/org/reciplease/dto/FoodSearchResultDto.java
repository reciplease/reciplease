package org.reciplease.dto;

import org.reciplease.model.Nutrients;
import org.reciplease.service.FoodSearchService.FoodSearchResult;

/** One entry in {@code GET /api/food/search} or {@code GET /api/food/barcode/{barcode}}'s response. */
public record FoodSearchResultDto(String source, String displayName, String identifiedFoodId, Nutrients nutrients) {

    public static FoodSearchResultDto from(final FoodSearchResult result) {
        return new FoodSearchResultDto(result.source().name(), result.displayName(), result.identifiedFoodId(), result.nutrients());
    }
}
