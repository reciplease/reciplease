package org.reciplease.service;

import lombok.RequiredArgsConstructor;
import org.reciplease.model.Nutrients;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Composes {@link FoodConsumptionLoggerPort} and {@link FoodCatalogPort} into a single "what
 * food did I mean" search: the user's own logged-food history (fuzzy-matched in-process, since
 * the port itself has no search capability of its own) plus a food-catalog search, merged into
 * one result list grouped by source. Neither port failing/being unavailable should fail the
 * other — history is simply skipped when the user has no linked consumption-logging account,
 * and {@link FoodCatalogPort} is itself documented best-effort (empty on provider failure).
 */
@Service
@RequiredArgsConstructor
public class FoodSearchService {

    private final FoodConsumptionLoggerPort foodConsumptionLoggerPort;
    private final FoodCatalogPort foodCatalogPort;

    public enum FoodSearchResultSource { HISTORY, CATALOG }

    public record FoodSearchResult(
            FoodSearchResultSource source,
            String displayName,
            String identifiedFoodId,
            Nutrients nutrients) {}

    public List<FoodSearchResult> search(final String userId, final String query) {
        final var historyResults = historyResults(userId, query);
        final var catalogResults = foodCatalogPort.searchByName(query).stream()
                .map(entry -> new FoodSearchResult(FoodSearchResultSource.CATALOG, entry.displayName(), null, entry.nutrients()));
        return Stream.concat(historyResults, catalogResults).toList();
    }

    public Optional<FoodSearchResult> searchByBarcode(final String barcode) {
        return foodCatalogPort.lookupByBarcode(barcode)
                .map(entry -> new FoodSearchResult(FoodSearchResultSource.CATALOG, entry.displayName(), null, entry.nutrients()));
    }

    private Stream<FoodSearchResult> historyResults(final String userId, final String query) {
        if (!foodConsumptionLoggerPort.isConnected(userId)) {
            return Stream.empty();
        }
        return foodConsumptionLoggerPort.history(userId).stream()
                .filter(entry -> fuzzyMatches(entry.displayName(), query))
                .map(entry -> new FoodSearchResult(FoodSearchResultSource.HISTORY, entry.displayName(), entry.consumptionId(), null));
    }

    /** Case-insensitive: every whitespace-separated word in {@code query} must appear as a substring of {@code displayName}. */
    private static boolean fuzzyMatches(final String displayName, final String query) {
        final var name = displayName.toLowerCase();
        return Arrays.stream(query.toLowerCase().trim().split("\\s+"))
                .filter(word -> !word.isBlank())
                .allMatch(name::contains);
    }
}
