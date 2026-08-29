package org.reciplease.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import org.reciplease.model.Nutrients;
import org.reciplease.service.FoodSearchService.FoodSearchResult;

/** One entry in {@code GET /api/food/search} or {@code GET /api/food/barcode/{barcode}}'s response. */
public record FoodSearchResultDto(
        @Schema(requiredMode = REQUIRED) String source,
        @Schema(requiredMode = REQUIRED) String displayName,
        String identifiedFoodId,
        @Schema(requiredMode = REQUIRED) Nutrients nutrients) {

    public static FoodSearchResultDto from(final FoodSearchResult result) {
        return new FoodSearchResultDto(
                result.source().name(), result.displayName(), result.identifiedFoodId(), result.nutrients());
    }
}
