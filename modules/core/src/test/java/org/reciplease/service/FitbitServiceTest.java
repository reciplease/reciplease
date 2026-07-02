package org.reciplease.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.reciplease.model.FitbitConnection;
import org.reciplease.repository.FitbitConnectionRepository;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@MockitoSettings
class FitbitServiceTest {

    private static final String CLIENT_ID = "test-client-id";
    private static final String CLIENT_SECRET = "test-client-secret";
    private static final String REDIRECT_URI = "https://next.reciplease.org/fitbit/callback";
    private static final String USER_ID = "user-1";

    @Mock
    private FitbitConnectionRepository fitbitConnectionRepository;

    private Instant now;
    private Clock clock;
    private RestClient.Builder restClientBuilder;
    private MockRestServiceServer mockServer;
    private FitbitService fitbitService;

    @BeforeEach
    void setUp() {
        now = Instant.parse("2026-07-02T12:00:00Z");
        clock = Clock.fixed(now, ZoneOffset.UTC);
        restClientBuilder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(restClientBuilder).build();
        fitbitService = new FitbitService(fitbitConnectionRepository, clock, restClientBuilder.build(), CLIENT_ID, CLIENT_SECRET, REDIRECT_URI);
    }

    private static String expectedBasicAuth() {
        return "Basic " + Base64.getEncoder().encodeToString((CLIENT_ID + ":" + CLIENT_SECRET).getBytes());
    }

    @Test
    @DisplayName("exchangeCode posts the authorization code grant and persists the resulting connection")
    void exchangeCodeHappyPath() {
        mockServer.expect(requestTo("https://api.fitbit.com/oauth2/token"))
                .andExpect(method(org.springframework.http.HttpMethod.POST))
                .andExpect(header("Authorization", expectedBasicAuth()))
                .andExpect(content().contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(content().string(org.hamcrest.Matchers.allOf(
                        org.hamcrest.Matchers.containsString("grant_type=authorization_code"),
                        org.hamcrest.Matchers.containsString("code=auth-code-1"),
                        org.hamcrest.Matchers.containsString("code_verifier=verifier-1"),
                        org.hamcrest.Matchers.containsString("client_id=" + CLIENT_ID))))
                .andRespond(withSuccess("""
                        {"access_token":"access-1","refresh_token":"refresh-1","expires_in":28800,"user_id":"fitbit-user-1"}""",
                        MediaType.APPLICATION_JSON));

        when(fitbitConnectionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        final var connection = fitbitService.exchangeCode(USER_ID, "auth-code-1", "verifier-1");

        mockServer.verify();
        assertThat(connection.userId(), is(USER_ID));
        assertThat(connection.accessToken(), is("access-1"));
        assertThat(connection.refreshToken(), is("refresh-1"));
        assertThat(connection.fitbitUserId(), is("fitbit-user-1"));
        assertThat(connection.expiresAt(), is(now.plusSeconds(28800)));
        assertThat(connection.createdAt(), is(now));
        assertThat(connection.updatedAt(), is(now));
    }

    @Test
    @DisplayName("searchFoods uses the stored access token without refreshing when not expired")
    void searchFoodsUsesStoredTokenWhenNotExpired() {
        final var connection = new FitbitConnection(USER_ID, "valid-access-token", "refresh-1",
                now.plusSeconds(3600), "fitbit-user-1", now.minusSeconds(60), now.minusSeconds(60));
        when(fitbitConnectionRepository.findByUserId(USER_ID)).thenReturn(Optional.of(connection));

        mockServer.expect(requestTo("https://api.fitbit.com/1/foods/search.json?query=banana"))
                .andExpect(method(org.springframework.http.HttpMethod.GET))
                .andExpect(header("Authorization", "Bearer valid-access-token"))
                .andRespond(withSuccess("{\"foods\":[]}", MediaType.APPLICATION_JSON));

        final var body = fitbitService.searchFoods(USER_ID, "banana");

        mockServer.verify();
        assertThat(body, is("{\"foods\":[]}"));
        verify(fitbitConnectionRepository, never()).save(any());
    }

    @Test
    @DisplayName("searchFoods transparently refreshes an expired token before calling the API")
    void searchFoodsRefreshesExpiredToken() {
        final var expired = new FitbitConnection(USER_ID, "stale-access-token", "refresh-1",
                now.minusSeconds(1), "fitbit-user-1", now.minusSeconds(3600), now.minusSeconds(3600));
        when(fitbitConnectionRepository.findByUserId(USER_ID)).thenReturn(Optional.of(expired));
        when(fitbitConnectionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        mockServer.expect(requestTo("https://api.fitbit.com/oauth2/token"))
                .andExpect(method(org.springframework.http.HttpMethod.POST))
                .andExpect(header("Authorization", expectedBasicAuth()))
                .andExpect(content().string(org.hamcrest.Matchers.allOf(
                        org.hamcrest.Matchers.containsString("grant_type=refresh_token"),
                        org.hamcrest.Matchers.containsString("refresh_token=refresh-1"))))
                .andRespond(withSuccess("""
                        {"access_token":"fresh-access-token","refresh_token":"fresh-refresh-token","expires_in":28800,"user_id":"fitbit-user-1"}""",
                        MediaType.APPLICATION_JSON));
        mockServer.expect(requestTo("https://api.fitbit.com/1/foods/search.json?query=banana"))
                .andExpect(method(org.springframework.http.HttpMethod.GET))
                .andExpect(header("Authorization", "Bearer fresh-access-token"))
                .andRespond(withSuccess("{\"foods\":[]}", MediaType.APPLICATION_JSON));

        fitbitService.searchFoods(USER_ID, "banana");

        mockServer.verify();
        verify(fitbitConnectionRepository).save(org.mockito.ArgumentMatchers.argThat(saved ->
                saved.accessToken().equals("fresh-access-token") && saved.refreshToken().equals("fresh-refresh-token")));
    }

    @Test
    @DisplayName("logFood sends foodId/unitId/amount/mealTypeId/date as query params, not a JSON body")
    void logFoodSendsExpectedParams() {
        final var connection = new FitbitConnection(USER_ID, "valid-access-token", "refresh-1",
                now.plusSeconds(3600), "fitbit-user-1", now.minusSeconds(60), now.minusSeconds(60));
        when(fitbitConnectionRepository.findByUserId(USER_ID)).thenReturn(Optional.of(connection));

        mockServer.expect(requestTo(
                        "https://api.fitbit.com/1/user/-/foods/log.json?foodId=food-1&mealTypeId=1&unitId=147&amount=2.5&date=2026-07-02"))
                .andExpect(method(org.springframework.http.HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer valid-access-token"))
                .andRespond(withSuccess());

        fitbitService.logFood(USER_ID, "food-1", "147", 2.5, "1", LocalDate.of(2026, 7, 2));

        mockServer.verify();
    }

    @Test
    @DisplayName("throws when logging food for a user with no linked Fitbit account")
    void logFoodThrowsWhenNotConnected() {
        when(fitbitConnectionRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

        assertThrows(FitbitNotConnectedException.class,
                () -> fitbitService.logFood(USER_ID, "food-1", "147", 2.5, "1", LocalDate.of(2026, 7, 2)));
    }

    @Test
    @DisplayName("connectionStatus and disconnect delegate to the repository")
    void connectionStatusAndDisconnectDelegate() {
        final var connection = new FitbitConnection(USER_ID, "a", "r", now, "fbu", now, now);
        when(fitbitConnectionRepository.findByUserId(USER_ID)).thenReturn(Optional.of(connection));

        assertThat(fitbitService.connectionStatus(USER_ID), is(Optional.of(connection)));

        fitbitService.disconnect(USER_ID);

        verify(fitbitConnectionRepository).deleteByUserId(USER_ID);
    }
}
