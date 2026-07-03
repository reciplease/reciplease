package org.reciplease.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.reciplease.model.FoodCatalogEntry;
import org.reciplease.model.Nutrients;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Optional;

/**
 * Looks up foods on Open Food Facts (world.openfoodfacts.org) — implements core's {@link
 * FoodCatalogPort}. Stateless: nothing is persisted locally.
 * <p>
 * Barcode lookup reuses the same v2 product-by-barcode endpoint the frontend's
 * {@code reciplease-nextjs/src/lib/openfoodfacts.ts} already calls for inventory scanning
 * (adding a {@code nutriments} field request, which that name/measure-only use case never
 * needed). Name search uses the legacy {@code /cgi/search.pl} endpoint instead of {@code
 * /api/v2/search}, since v2 only supports structured tag/category filtering — full-text search
 * by name is a v1-only capability. Both this search-by-name capability and the exact {@code
 * nutriments} field names below are new to this codebase and should be re-confirmed against a
 * live call before shipping, the same diligence {@code GoogleHealthAdapter} applied to its own
 * endpoint quirks.
 */
@Service
public class OpenFoodFactsAdapter implements FoodCatalogPort {

    private static final String SEARCH_URL = "https://world.openfoodfacts.org/cgi/search.pl"
            + "?search_terms={query}&search_simple=1&action=process&json=1&page_size=20"
            + "&fields=code,product_name,brands,nutriments";
    private static final String BARCODE_URL = "https://world.openfoodfacts.org/api/v2/product/{barcode}"
            + "?fields=code,product_name,brands,nutriments";

    private final RestClient restClient;

    public OpenFoodFactsAdapter() {
        this(RestClient.builder().build());
    }

    /** Test-only entry point letting {@link org.springframework.test.web.client.MockRestServiceServer} bind. */
    OpenFoodFactsAdapter(final RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public List<FoodCatalogEntry> searchByName(final String query) {
        try {
            final var response = restClient.get()
                    .uri(SEARCH_URL, query)
                    .retrieve()
                    .body(SearchResponse.class);
            if (response == null || response.products() == null) {
                return List.of();
            }
            return response.products().stream()
                    .filter(p -> p.productName() != null && !p.productName().isBlank())
                    .map(OpenFoodFactsAdapter::toCatalogEntry)
                    .toList();
        } catch (final RestClientException e) {
            return List.of();
        }
    }

    @Override
    public Optional<FoodCatalogEntry> lookupByBarcode(final String barcode) {
        try {
            final var response = restClient.get()
                    .uri(BARCODE_URL, barcode)
                    .retrieve()
                    .body(ProductLookupResponse.class);
            if (response == null || response.status() != 1 || response.product() == null) {
                return Optional.empty();
            }
            return Optional.of(toCatalogEntry(response.product()));
        } catch (final RestClientException e) {
            return Optional.empty();
        }
    }

    private static FoodCatalogEntry toCatalogEntry(final Product product) {
        return new FoodCatalogEntry(product.code(), product.productName(), firstBrand(product.brands()), toNutrients(product.nutriments()));
    }

    private static String firstBrand(final String brands) {
        return brands == null ? null : brands.split(",")[0].trim();
    }

    private static Nutrients toNutrients(final Nutriments nutriments) {
        if (nutriments == null) {
            return null;
        }
        return new Nutrients(nutriments.energyKcal100g(), nutriments.proteins100g(), nutriments.fat100g(), nutriments.carbohydrates100g());
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record SearchResponse(@JsonProperty("products") List<Product> products) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record ProductLookupResponse(@JsonProperty("status") int status, @JsonProperty("product") Product product) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Product(
            @JsonProperty("code") String code,
            @JsonProperty("product_name") String productName,
            @JsonProperty("brands") String brands,
            @JsonProperty("nutriments") Nutriments nutriments) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Nutriments(
            @JsonProperty("energy-kcal_100g") Double energyKcal100g,
            @JsonProperty("proteins_100g") Double proteins100g,
            @JsonProperty("fat_100g") Double fat100g,
            @JsonProperty("carbohydrates_100g") Double carbohydrates100g) {}
}
