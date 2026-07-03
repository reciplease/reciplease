package org.reciplease.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.reciplease.model.GoogleHealthConnection;
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
class GoogleHealthServiceTest {

    private static final String USER_ID = "user-1";

    @Mock
    private GoogleHealthConnectionRepository googleHealthConnectionRepository;

    private Instant now;
    private Clock clock;
    private RestClient.Builder restClientBuilder;
    private MockRestServiceServer mockServer;
    private GoogleHealthService googleHealthService;

    @BeforeEach
    void setUp() {
        now = Instant.parse("2026-07-02T12:00:00Z");
        clock = Clock.fixed(now, ZoneOffset.UTC);
        restClientBuilder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(restClientBuilder).build();
        googleHealthService = new GoogleHealthService(googleHealthConnectionRepository, clock, restClientBuilder.build());
    }

    @Test
    @DisplayName("storeConnection creates a new connection, using now as createdAt, when none exists yet")
    void storeConnectionCreatesNewConnection() {
        when(googleHealthConnectionRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());
        when(googleHealthConnectionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        final var connection = googleHealthService.storeConnection(USER_ID, "access-1", "refresh-1", 3600, "nutrition.writeonly");

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

        final var connection = googleHealthService.storeConnection(USER_ID, "fresh-access", "fresh-refresh", 7200, "nutrition.readonly");

        assertThat(connection.accessToken(), is("fresh-access"));
        assertThat(connection.refreshToken(), is("fresh-refresh"));
        assertThat(connection.expiresAt(), is(now.plusSeconds(7200)));
        assertThat(connection.createdAt(), is(originalCreatedAt));
        assertThat(connection.updatedAt(), is(now));
    }

    @Test
    @DisplayName("searchFoods uses the stored access token as-is, with no expiry check or refresh")
    void searchFoodsUsesStoredToken() {
        final var connection = new GoogleHealthConnection(USER_ID, "valid-access-token", "refresh-1",
                now.plusSeconds(3600), "nutrition.readonly", now.minusSeconds(60), now.minusSeconds(60));
        when(googleHealthConnectionRepository.findByUserId(USER_ID)).thenReturn(Optional.of(connection));

        mockServer.expect(requestTo("https://health.googleapis.com/v4/users/me/dataTypes/food/dataPoints?filter=food.food_display_name%3A%22banana%22"))
                .andExpect(method(org.springframework.http.HttpMethod.GET))
                .andExpect(header("Authorization", "Bearer valid-access-token"))
                .andRespond(withSuccess("{\"dataPoint\":[]}", MediaType.APPLICATION_JSON));

        final var body = googleHealthService.searchFoods(USER_ID, "banana");

        mockServer.verify();
        assertThat(body, is("{\"dataPoint\":[]}"));
        verify(googleHealthConnectionRepository, never()).save(any());
    }

    @Test
    @DisplayName("logFood PATCHes a NutritionLog data point referencing the identified food")
    void logFoodSendsExpectedBodyForIdentifiedFood() {
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
                .andRespond(withSuccess());

        googleHealthService.logFood(USER_ID, "food-1", "Banana", "LUNCH", 2.5, LocalDate.of(2026, 7, 2));

        mockServer.verify();
    }

    @Test
    @DisplayName("throws when logging food for a user with no linked Google Health account")
    void logFoodThrowsWhenNotConnected() {
        when(googleHealthConnectionRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

        assertThrows(GoogleHealthNotConnectedException.class,
                () -> googleHealthService.logFood(USER_ID, "food-1", "Banana", "LUNCH", 2.5, LocalDate.of(2026, 7, 2)));
    }

    @Test
    @DisplayName("connectionStatus and disconnect delegate to the repository")
    void connectionStatusAndDisconnectDelegate() {
        final var connection = new GoogleHealthConnection(USER_ID, "a", "r", now, "nutrition.readonly", now, now);
        when(googleHealthConnectionRepository.findByUserId(USER_ID)).thenReturn(Optional.of(connection));

        assertThat(googleHealthService.connectionStatus(USER_ID), is(Optional.of(connection)));

        googleHealthService.disconnect(USER_ID);

        verify(googleHealthConnectionRepository).deleteByUserId(USER_ID);
    }
}
