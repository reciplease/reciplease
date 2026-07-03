package org.reciplease.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.reciplease.model.FoodConsumption;
import org.reciplease.model.GoogleHealthConnection;
import org.reciplease.model.MealType;
import org.reciplease.model.Nutrients;
import org.reciplease.repository.GoogleHealthConnectionRepository;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@MockitoSettings
class GoogleHealthAdapterTest {

    private static final String USER_ID = "user-1";

    @Mock
    private GoogleHealthConnectionRepository googleHealthConnectionRepository;

    private Instant now;
    private Clock clock;
    private RestClient.Builder restClientBuilder;
    private MockRestServiceServer mockServer;
    private GoogleHealthAdapter googleHealthAdapter;

    @BeforeEach
    void setUp() {
        now = Instant.parse("2026-07-02T12:00:00Z");
        clock = Clock.fixed(now, ZoneOffset.UTC);
        restClientBuilder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(restClientBuilder).build();
        googleHealthAdapter = new GoogleHealthAdapter(googleHealthConnectionRepository, clock, restClientBuilder.build());
    }

    @Test
    @DisplayName("storeConnection creates a new connection, using now as createdAt, when none exists yet")
    void storeConnectionCreatesNewConnection() {
        when(googleHealthConnectionRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());
        when(googleHealthConnectionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        final var connection = googleHealthAdapter.storeConnection(USER_ID, "access-1", "refresh-1", 3600, "nutrition.writeonly");

        assertThat(connection.userId(), is(USER_ID));
        assertThat(connection.accessToken(), is("access-1"));
        assertThat(connection.refreshToken(), is("refresh-1"));
        assertThat(connection.scope(), is("nutrition.writeonly"));
        assertThat(connection.expiresAt(), is(now.plusSeconds(3600)));
        assertThat(connection.createdAt(), is(now));
        assertThat(connection.updatedAt(), is(now));
    }

    @Test
    @DisplayName("storeConnection upserts an existing connection, preserving createdAt but replacing tokens/expiresAt/updatedAt")
    void storeConnectionUpdatesExistingConnectionPreservingCreatedAt() {
        final var originalCreatedAt = now.minusSeconds(86_400);
        final var existing = new GoogleHealthConnection(USER_ID, "stale-access", "stale-refresh",
                now.minusSeconds(1), "nutrition.readonly", originalCreatedAt, now.minusSeconds(3600));
        when(googleHealthConnectionRepository.findByUserId(USER_ID)).thenReturn(Optional.of(existing));
        when(googleHealthConnectionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        final var connection = googleHealthAdapter.storeConnection(USER_ID, "fresh-access", "fresh-refresh", 7200, "nutrition.readonly");

        assertThat(connection.accessToken(), is("fresh-access"));
        assertThat(connection.refreshToken(), is("fresh-refresh"));
        assertThat(connection.expiresAt(), is(now.plusSeconds(7200)));
        assertThat(connection.createdAt(), is(originalCreatedAt));
        assertThat(connection.updatedAt(), is(now));
    }

    @Test
    @DisplayName("isConnected reflects whether the repository has a connection for the user")
    void isConnectedReflectsRepository() {
        when(googleHealthConnectionRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());
        assertThat(googleHealthAdapter.isConnected(USER_ID), is(false));

        when(googleHealthConnectionRepository.findByUserId(USER_ID)).thenReturn(Optional.of(
                new GoogleHealthConnection(USER_ID, "a", "r", now, "nutrition.readonly", now, now)));
        assertThat(googleHealthAdapter.isConnected(USER_ID), is(true));
    }

    @Test
    @DisplayName("history lists the user's nutrition-log data points, mapping identified-food entries' food id and anonymous entries' display name")
    void historyListsAndMapsDataPoints() {
        final var connection = new GoogleHealthConnection(USER_ID, "valid-access-token", "refresh-1",
                now.plusSeconds(3600), "nutrition.readonly", now.minusSeconds(60), now.minusSeconds(60));
        when(googleHealthConnectionRepository.findByUserId(USER_ID)).thenReturn(Optional.of(connection));

        mockServer.expect(requestTo("https://health.googleapis.com/v4/users/me/dataTypes/nutrition-log/dataPoints"))
                .andExpect(method(org.springframework.http.HttpMethod.GET))
                .andExpect(header("Authorization", "Bearer valid-access-token"))
                .andRespond(withSuccess("""
                        {"dataPoint": [
                            {"food": "food/food-1", "foodDisplayName": "Banana"},
                            {"foodDisplayName": "Homemade Soup"}
                        ]}""", MediaType.APPLICATION_JSON));

        final var history = googleHealthAdapter.history(USER_ID);

        mockServer.verify();
        assertThat(history, contains(
                new org.reciplease.model.LoggedFoodHistoryEntry("food-1", "Banana"),
                new org.reciplease.model.LoggedFoodHistoryEntry(null, "Homemade Soup")));
        verify(googleHealthConnectionRepository, never()).save(any());
    }

    @Test
    @DisplayName("log PATCHes a NutritionLog data point referencing the identified food")
    void logSendsExpectedBodyForIdentifiedFood() {
        final var connection = new GoogleHealthConnection(USER_ID, "valid-access-token", "refresh-1",
                now.plusSeconds(3600), "nutrition.writeonly", now.minusSeconds(60), now.minusSeconds(60));
        when(googleHealthConnectionRepository.findByUserId(USER_ID)).thenReturn(Optional.of(connection));

        mockServer.expect(requestTo(org.hamcrest.Matchers.startsWith(
                        "https://health.googleapis.com/v4/users/me/dataTypes/nutrition-log/dataPoints/")))
                .andExpect(method(org.springframework.http.HttpMethod.PATCH))
                .andExpect(header("Authorization", "Bearer valid-access-token"))
                .andExpect(org.springframework.test.web.client.match.MockRestRequestMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath("$.food").value(is("food/food-1")))
                .andExpect(org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath("$.mealType").value(is("LUNCH")))
                .andExpect(org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath("$.amount").value(is(2.5)))
                .andExpect(org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath("$.nutrients").doesNotExist())
                .andRespond(withSuccess());

        googleHealthAdapter.log(new FoodConsumption(USER_ID, Optional.of("food-1"), "Banana", Optional.empty(),
                MealType.LUNCH, 2.5, LocalDate.of(2026, 7, 2)));

        mockServer.verify();
    }

    @Test
    @DisplayName("log PATCHes an anonymous NutritionLog data point with nutrients when picked from a catalog result")
    void logSendsNutrientsForAnonymousFood() {
        final var connection = new GoogleHealthConnection(USER_ID, "valid-access-token", "refresh-1",
                now.plusSeconds(3600), "nutrition.writeonly", now.minusSeconds(60), now.minusSeconds(60));
        when(googleHealthConnectionRepository.findByUserId(USER_ID)).thenReturn(Optional.of(connection));

        mockServer.expect(requestTo(org.hamcrest.Matchers.startsWith(
                        "https://health.googleapis.com/v4/users/me/dataTypes/nutrition-log/dataPoints/")))
                .andExpect(method(org.springframework.http.HttpMethod.PATCH))
                .andExpect(org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath("$.food").doesNotExist())
                .andExpect(org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath("$.foodDisplayName").value(is("Greek Yogurt")))
                .andExpect(org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath("$.nutrients.energy").value(is(120.0)))
                .andExpect(org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath("$.nutrients.protein").value(is(10.0)))
                .andRespond(withSuccess());

        googleHealthAdapter.log(new FoodConsumption(USER_ID, Optional.empty(), "Greek Yogurt",
                Optional.of(new Nutrients(120.0, 10.0, 3.0, 5.0)), MealType.SNACK, 1.0, LocalDate.of(2026, 7, 2)));

        mockServer.verify();
    }

    @Test
    @DisplayName("throws when logging food for a user with no linked Google Health account")
    void logThrowsWhenNotConnected() {
        when(googleHealthConnectionRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

        assertThrows(FoodConsumptionLoggerNotConnectedException.class,
                () -> googleHealthAdapter.log(new FoodConsumption(USER_ID, Optional.of("food-1"), "Banana",
                        Optional.empty(), MealType.LUNCH, 2.5, LocalDate.of(2026, 7, 2))));
    }

    @Test
    @DisplayName("connectionStatus and disconnect delegate to the repository")
    void connectionStatusAndDisconnectDelegate() {
        final var connection = new GoogleHealthConnection(USER_ID, "a", "r", now, "nutrition.readonly", now, now);
        when(googleHealthConnectionRepository.findByUserId(USER_ID)).thenReturn(Optional.of(connection));

        assertThat(googleHealthAdapter.connectionStatus(USER_ID), is(Optional.of(connection)));

        googleHealthAdapter.disconnect(USER_ID);

        verify(googleHealthConnectionRepository).deleteByUserId(USER_ID);
    }
}
