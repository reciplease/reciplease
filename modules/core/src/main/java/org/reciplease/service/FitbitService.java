package org.reciplease.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.reciplease.model.FitbitConnection;
import org.reciplease.repository.FitbitConnectionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Base64;
import java.util.Optional;

/**
 * Talks to the legacy Fitbit Web API (OAuth2 token exchange/refresh, food search, food
 * logging) on behalf of a Reciplease user, and persists/refreshes the resulting tokens via
 * {@link FitbitConnectionRepository}. Uses Spring's synchronous {@link RestClient} — there is
 * no other outbound HTTP client anywhere else in this codebase yet.
 */
@Service
public class FitbitService {

    private static final String TOKEN_URL = "https://api.fitbit.com/oauth2/token";
    private static final String SEARCH_URL = "https://api.fitbit.com/1/foods/search.json";
    private static final String LOG_URL = "https://api.fitbit.com/1/user/-/foods/log.json";

    private final FitbitConnectionRepository fitbitConnectionRepository;
    private final Clock clock;
    private final RestClient restClient;
    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;

    @Autowired
    public FitbitService(final FitbitConnectionRepository fitbitConnectionRepository,
                          final Clock clock,
                          @Value("${reciplease.fitbit.client-id}") final String clientId,
                          @Value("${reciplease.fitbit.client-secret}") final String clientSecret,
                          @Value("${reciplease.fitbit.redirect-uri}") final String redirectUri) {
        // Built directly rather than injecting a Spring-provided RestClient.Builder bean,
        // since that bean is only auto-configured in a full web application context — the
        // Cucumber features module boots this service in a plain context without it.
        this(fitbitConnectionRepository, clock, RestClient.builder().build(), clientId, clientSecret, redirectUri);
    }

    /**
     * Test-only entry point letting {@link org.springframework.test.web.client.MockRestServiceServer}
     * bind to a pre-built {@link RestClient} instead of the one the public constructor builds.
     */
    FitbitService(final FitbitConnectionRepository fitbitConnectionRepository,
                  final Clock clock,
                  final RestClient restClient,
                  final String clientId,
                  final String clientSecret,
                  final String redirectUri) {
        this.fitbitConnectionRepository = fitbitConnectionRepository;
        this.clock = clock;
        this.restClient = restClient;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
    }

    /**
     * Exchanges an authorization {@code code} (from the frontend-driven Fitbit OAuth2/PKCE
     * redirect) for access/refresh tokens, and persists them as {@code userId}'s connection.
     */
    public FitbitConnection exchangeCode(final String userId, final String code, final String codeVerifier) {
        final var form = new LinkedMultiValueMap<String, String>();
        form.add("grant_type", "authorization_code");
        form.add("code", code);
        form.add("redirect_uri", redirectUri);
        form.add("code_verifier", codeVerifier);
        form.add("client_id", clientId);

        final var tokenResponse = requestToken(form);
        final var now = clock.instant();
        final var connection = new FitbitConnection(userId, tokenResponse.accessToken(), tokenResponse.refreshToken(),
                now.plusSeconds(tokenResponse.expiresIn()), tokenResponse.userId(), now, now);
        return fitbitConnectionRepository.save(connection);
    }

    public Optional<FitbitConnection> connectionStatus(final String userId) {
        return fitbitConnectionRepository.findByUserId(userId);
    }

    public void disconnect(final String userId) {
        fitbitConnectionRepository.deleteByUserId(userId);
    }

    /** Searches Fitbit's food database, returning its response body as-is (raw JSON). */
    public String searchFoods(final String userId, final String query) {
        final var connection = ensureFreshConnection(userId);
        return restClient.get()
                .uri(SEARCH_URL + "?query={query}", query)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + connection.accessToken())
                .retrieve()
                .body(String.class);
    }

    /**
     * Logs a food-eaten entry against {@code userId}'s Fitbit food log. Fitbit's legacy API
     * expects these as query/form params, not a JSON body.
     */
    public void logFood(final String userId, final String foodId, final String unitId, final double amount,
                         final String mealTypeId, final LocalDate date) {
        final var connection = ensureFreshConnection(userId);
        restClient.post()
                .uri(LOG_URL + "?foodId={foodId}&mealTypeId={mealTypeId}&unitId={unitId}&amount={amount}&date={date}",
                        foodId, mealTypeId, unitId, amount, date)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + connection.accessToken())
                .retrieve()
                .toBodilessEntity();
    }

    /** Returns {@code userId}'s connection with a still-valid access token, refreshing it first if expired. */
    private FitbitConnection ensureFreshConnection(final String userId) {
        final var connection = fitbitConnectionRepository.findByUserId(userId)
                .orElseThrow(() -> new FitbitNotConnectedException(userId));
        if (connection.expiresAt().isAfter(clock.instant())) {
            return connection;
        }
        return refresh(connection);
    }

    private FitbitConnection refresh(final FitbitConnection connection) {
        final var form = new LinkedMultiValueMap<String, String>();
        form.add("grant_type", "refresh_token");
        form.add("refresh_token", connection.refreshToken());

        final var tokenResponse = requestToken(form);
        final var now = clock.instant();
        final var fitbitUserId = tokenResponse.userId() != null ? tokenResponse.userId() : connection.fitbitUserId();
        final var refreshed = new FitbitConnection(connection.userId(), tokenResponse.accessToken(), tokenResponse.refreshToken(),
                now.plusSeconds(tokenResponse.expiresIn()), fitbitUserId, connection.createdAt(), now);
        return fitbitConnectionRepository.save(refreshed);
    }

    private TokenResponse requestToken(final MultiValueMap<String, String> form) {
        return restClient.post()
                .uri(TOKEN_URL)
                .header(HttpHeaders.AUTHORIZATION, basicAuth())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(TokenResponse.class);
    }

    private String basicAuth() {
        final var credentials = clientId + ":" + clientSecret;
        return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
    }

    private record TokenResponse(
            @JsonProperty("access_token") String accessToken,
            @JsonProperty("refresh_token") String refreshToken,
            @JsonProperty("expires_in") long expiresIn,
            @JsonProperty("user_id") String userId) {}
}
