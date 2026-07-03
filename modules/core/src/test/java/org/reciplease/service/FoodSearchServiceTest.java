package org.reciplease.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reciplease.model.FoodCatalogEntry;
import org.reciplease.model.LoggedFoodHistoryEntry;
import org.reciplease.model.Nutrients;
import org.reciplease.service.FoodSearchService.FoodSearchResult;
import org.reciplease.service.FoodSearchService.FoodSearchResultSource;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FoodSearchServiceTest {

    private static final String USER_ID = "user-1";

    @Mock
    private FoodConsumptionLoggerPort foodConsumptionLoggerPort;
    @Mock
    private FoodCatalogPort foodCatalogPort;

    private FoodSearchService service() {
        return new FoodSearchService(foodConsumptionLoggerPort, foodCatalogPort);
    }

    @Test
    @DisplayName("search merges fuzzy-matched history results with catalog results")
    void searchMergesHistoryAndCatalog() {
        when(foodConsumptionLoggerPort.isConnected(USER_ID)).thenReturn(true);
        when(foodConsumptionLoggerPort.history(USER_ID)).thenReturn(List.of(
                new LoggedFoodHistoryEntry("food-1", "Chicken Breast, grilled"),
                new LoggedFoodHistoryEntry(null, "Homemade Soup")));
        when(foodCatalogPort.searchByName("chick")).thenReturn(List.of(
                new FoodCatalogEntry("123", "Chicken Tikka Masala", "Trader Joe's", new Nutrients(200.0, 15.0, 8.0, 10.0))));

        final var results = service().search(USER_ID, "chick");

        assertThat(results, contains(
                new FoodSearchResult(FoodSearchResultSource.HISTORY, "Chicken Breast, grilled", "food-1", null),
                new FoodSearchResult(FoodSearchResultSource.CATALOG, "Chicken Tikka Masala", null,
                        new Nutrients(200.0, 15.0, 8.0, 10.0))));
    }

    @Test
    @DisplayName("search skips history entirely when the user has no linked consumption-logging account")
    void searchSkipsHistoryWhenNotConnected() {
        when(foodConsumptionLoggerPort.isConnected(USER_ID)).thenReturn(false);
        when(foodCatalogPort.searchByName("yogurt")).thenReturn(List.of(
                new FoodCatalogEntry("1", "Greek Yogurt", "Chobani", null)));

        final var results = service().search(USER_ID, "yogurt");

        assertThat(results, contains(new FoodSearchResult(FoodSearchResultSource.CATALOG, "Greek Yogurt", null, null)));
    }

    @Test
    @DisplayName("search's fuzzy match is case-insensitive and requires every query word to appear")
    void fuzzyMatchIsCaseInsensitiveAndRequiresEveryWord() {
        when(foodConsumptionLoggerPort.isConnected(USER_ID)).thenReturn(true);
        when(foodConsumptionLoggerPort.history(USER_ID)).thenReturn(List.of(
                new LoggedFoodHistoryEntry("food-1", "Greek Yogurt, Plain"),
                new LoggedFoodHistoryEntry("food-2", "Banana")));
        when(foodCatalogPort.searchByName("GREEK yogurt")).thenReturn(List.of());

        final var results = service().search(USER_ID, "GREEK yogurt");

        assertThat(results, contains(new FoodSearchResult(FoodSearchResultSource.HISTORY, "Greek Yogurt, Plain", "food-1", null)));
    }

    @Test
    @DisplayName("searchByBarcode delegates to the catalog port only")
    void searchByBarcodeDelegatesToCatalogOnly() {
        when(foodCatalogPort.lookupByBarcode("012345")).thenReturn(
                Optional.of(new FoodCatalogEntry("012345", "Oat Milk", "Oatly", new Nutrients(45.0, 1.0, 1.5, 6.0))));

        final var result = service().searchByBarcode("012345");

        assertThat(result, is(Optional.of(new FoodSearchResult(FoodSearchResultSource.CATALOG, "Oat Milk", null,
                new Nutrients(45.0, 1.0, 1.5, 6.0)))));
    }

    @Test
    @DisplayName("searchByBarcode returns empty when the catalog has no match")
    void searchByBarcodeReturnsEmptyWhenNotFound() {
        when(foodCatalogPort.lookupByBarcode("000000")).thenReturn(Optional.empty());

        assertThat(service().searchByBarcode("000000"), is(Optional.empty()));
    }
}
