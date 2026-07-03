package org.reciplease.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.reciplease.model.FoodCatalogEntry;
import org.reciplease.model.Nutrients;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class OpenFoodFactsAdapterTest {

    private RestClient.Builder restClientBuilder;
    private MockRestServiceServer mockServer;
    private OpenFoodFactsAdapter openFoodFactsAdapter;

    @BeforeEach
    void setUp() {
        restClientBuilder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(restClientBuilder).build();
        openFoodFactsAdapter = new OpenFoodFactsAdapter(restClientBuilder.build());
    }

    @Test
    @DisplayName("searchByName maps matching products, parsing nutrients and the first brand")
    void searchByNameMapsProducts() {
        mockServer.expect(requestTo(org.hamcrest.Matchers.startsWith("https://world.openfoodfacts.org/cgi/search.pl?search_terms=yogurt")))
                .andRespond(withSuccess("""
                        {"products": [
                            {"code": "123", "product_name": "Greek Yogurt", "brands": "Chobani,Other",
                             "nutriments": {"energy-kcal_100g": 120.0, "proteins_100g": 10.0, "fat_100g": 3.0, "carbohydrates_100g": 5.0}}
                        ]}""", MediaType.APPLICATION_JSON));

        final var results = openFoodFactsAdapter.searchByName("yogurt");

        assertThat(results, contains(new FoodCatalogEntry("123", "Greek Yogurt", "Chobani", new Nutrients(120.0, 10.0, 3.0, 5.0))));
    }

    @Test
    @DisplayName("searchByName skips products with no name and returns empty on a server error")
    void searchByNameSkipsUnnamedProductsAndToleratesFailure() {
        mockServer.expect(requestTo(org.hamcrest.Matchers.startsWith("https://world.openfoodfacts.org/cgi/search.pl")))
                .andRespond(withSuccess("""
                        {"products": [{"code": "999", "product_name": ""}]}""", MediaType.APPLICATION_JSON));

        assertThat(openFoodFactsAdapter.searchByName("x"), is(java.util.List.of()));

        mockServer.reset();
        mockServer.expect(requestTo(org.hamcrest.Matchers.startsWith("https://world.openfoodfacts.org/cgi/search.pl")))
                .andRespond(withServerError());

        assertThat(openFoodFactsAdapter.searchByName("x"), is(java.util.List.of()));
    }

    @Test
    @DisplayName("lookupByBarcode returns the mapped product when found")
    void lookupByBarcodeReturnsProduct() {
        mockServer.expect(requestTo("https://world.openfoodfacts.org/api/v2/product/012345?fields=code,product_name,brands,nutriments"))
                .andRespond(withSuccess("""
                        {"status": 1, "product": {"code": "012345", "product_name": "Oat Milk", "brands": "Oatly",
                         "nutriments": {"energy-kcal_100g": 45.0, "proteins_100g": 1.0, "fat_100g": 1.5, "carbohydrates_100g": 6.0}}}""",
                        MediaType.APPLICATION_JSON));

        final var result = openFoodFactsAdapter.lookupByBarcode("012345");

        assertThat(result, is(java.util.Optional.of(new FoodCatalogEntry("012345", "Oat Milk", "Oatly", new Nutrients(45.0, 1.0, 1.5, 6.0)))));
    }

    @Test
    @DisplayName("lookupByBarcode returns empty when the product isn't found or the request fails")
    void lookupByBarcodeReturnsEmptyWhenMissingOrFailing() {
        mockServer.expect(requestTo("https://world.openfoodfacts.org/api/v2/product/000000?fields=code,product_name,brands,nutriments"))
                .andRespond(withSuccess("""
                        {"status": 0}""", MediaType.APPLICATION_JSON));

        assertThat(openFoodFactsAdapter.lookupByBarcode("000000"), is(java.util.Optional.empty()));

        mockServer.reset();
        mockServer.expect(requestTo("https://world.openfoodfacts.org/api/v2/product/000001?fields=code,product_name,brands,nutriments"))
                .andRespond(withServerError());

        assertThat(openFoodFactsAdapter.lookupByBarcode("000001"), is(java.util.Optional.empty()));
    }
}
