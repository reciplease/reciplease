package org.reciplease.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.reciplease.dto.FoodSearchResultDto;
import org.reciplease.service.FoodSearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * "What food did I mean" search for the Eat flow: merges the current user's own logged-food
 * history with a food-catalog (Open Food Facts) search/barcode lookup, via core's {@link
 * FoodSearchService}. Provider-agnostic by design — unlike {@link GoogleHealthController}, this
 * controller has no Google-specific vocabulary.
 */
@RestController
@RequestMapping("api/food")
@Tag(name = "Food Search")
@RequiredArgsConstructor
public class FoodSearchController {

    private final FoodSearchService foodSearchService;

    @GetMapping("search")
    @PreAuthorize("isAuthenticated()")
    @Operation(operationId = "searchFoods")
    public List<FoodSearchResultDto> search(@RequestParam final String query) {
        return foodSearchService.search(currentUserId(), query).stream()
                .map(FoodSearchResultDto::from)
                .toList();
    }

    @GetMapping("barcode/{barcode}")
    @PreAuthorize("isAuthenticated()")
    @Operation(operationId = "findFoodByBarcode")
    public ResponseEntity<FoodSearchResultDto> barcode(@PathVariable final String barcode) {
        return foodSearchService
                .searchByBarcode(barcode)
                .map(FoodSearchResultDto::from)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private String currentUserId() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
