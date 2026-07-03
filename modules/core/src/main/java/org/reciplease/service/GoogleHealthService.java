package org.reciplease.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.reciplease.model.GoogleHealthConnection;
import org.reciplease.repository.GoogleHealthConnectionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

/**
 * Talks to the Google Health API (food search, nutrition logging) on behalf of a Reciplease
 * user, and persists tokens pushed to it by the frontend via {@link
 * GoogleHealthConnectionRepository}. Google Health is the durable replacement for the
 * now-decommissioned legacy Fitbit Web API — same underlying Fitbit data model, rebuilt on
 * Google's infrastructure, authenticated via standard Google OAuth2 (incremental authorization
 * against the same OAuth client Reciplease already uses for Google sign-in) rather than
 * Fitbit's own OAuth2/PKCE flow.
 * <p>
 * The backend deliberately never talks to {@code oauth2.googleapis.com} and never holds the
 * Google OAuth client secret: the Next.js frontend performs the authorization-code exchange
 * and all token refreshes itself (it already holds the Google client id/secret for NextAuth
 * sign-in), then pushes the resulting access/refresh tokens here via {@link
 * #storeConnection(String, String, String, long, String)} to be persisted. Uses Spring's
 * synchronous {@link RestClient} — there is no other outbound HTTP client anywhere else in
 * this codebase yet.
 */
@Service
public class GoogleHealthService {

    private static final String FOOD_SEARCH_URL = "https://health.googleapis.com/v4/users/me/dataTypes/food/dataPoints";
    private static final String NUTRITION_LOG_URL = "https://health.googleapis.com/v4/users/me/dataTypes/nutrition-log/dataPoints/{dataPointId}";

    private final GoogleHealthConnectionRepository googleHealthConnectionRepository;
    private final Clock clock;
    private final RestClient restClient;

    @Autowired
    public GoogleHealthService(final GoogleHealthConnectionRepository googleHealthConnectionRepository,
                                final Clock clock) {
        // Built directly rather than injecting a Spring-provided RestClient.Builder bean,
        // since that bean is only auto-configured in a full web application context — the
        // Cucumber features module boots this service in a plain context without it.
        this(googleHealthConnectionRepository, clock, RestClient.builder().build());
    }

    /**
     * Test-only entry point letting {@link org.springframework.test.web.client.MockRestServiceServer}
     * bind to a pre-built {@link RestClient} instead of the one the public constructor builds.
     */
    GoogleHealthService(final GoogleHealthConnectionRepository googleHealthConnectionRepository,
                         final Clock clock,
                         final RestClient restClient) {
        this.googleHealthConnectionRepository = googleHealthConnectionRepository;
        this.clock = clock;
        this.restClient = restClient;
    }

    /**
     * Upserts {@code userId}'s Google Health connection with tokens the frontend obtained
     * itself (either the initial authorization-code exchange or a subsequent refresh) —
     * preserving the original {@code createdAt} across updates. Used both to link the account
     * for the first time and, every time the frontend proactively refreshes an expiring
     * access token, to push the refreshed tokens back for storage.
     */
    public GoogleHealthConnection storeConnection(final String userId, final String accessToken, final String refreshToken,
                                                    final long expiresIn, final String scope) {
        final var now = clock.instant();
        final var createdAt = googleHealthConnectionRepository.findByUserId(userId)
                .map(GoogleHealthConnection::createdAt)
                .orElse(now);
        final var connection = new GoogleHealthConnection(userId, accessToken, refreshToken,
                now.plusSeconds(expiresIn), scope, createdAt, now);
        return googleHealthConnectionRepository.save(connection);
    }

    public Optional<GoogleHealthConnection> connectionStatus(final String userId) {
        return googleHealthConnectionRepository.findByUserId(userId);
    }

    public void disconnect(final String userId) {
        googleHealthConnectionRepository.deleteByUserId(userId);
    }

    /**
     * Searches Google's food database, returning its response body as-is (raw JSON). Google
     * Health has no dedicated food-search endpoint; foods are queried via the generic
     * data-points list resource for the {@code food} data type.
     * <p>
     * The filter field name below ({@code food.display_name}) is confirmed against a live
     * 400 response from the API — an earlier guess of {@code food.food_display_name} was
     * rejected with "Member 'food.food_display_name' is not supported for filtering. Food
     * only supports 'food.display_name' and 'food.language_code'." The {@code :} contains-style
     * operator itself wasn't flagged as invalid, so it's kept as-is, but still unconfirmed.
     */
    public String searchFoods(final String userId, final String query) {
        final var connection = requireConnection(userId);
        return restClient.get()
                .uri(FOOD_SEARCH_URL + "?filter={filter}", "food.display_name:\"" + query + "\"")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + connection.accessToken())
                .retrieve()
                .body(String.class);
    }

    /**
     * Logs a food-eaten entry as a {@code NutritionLog} data point against {@code userId}'s
     * Google Health nutrition log. The data point id is client-generated (a fresh UUID per
     * call, per the Google Health API's PATCH-to-create-or-update contract).
     * <p>
     * When {@code foodId} identifies a specific Google Health {@code Food} resource (the
     * primary path — matches the UI flow of picking a search result), the data point
     * references that food and Google populates its nutrients/energy itself. Otherwise this
     * logs an "anonymous food" entry using just {@code foodDisplayName}, with no manually
     * computed nutrients — Reciplease doesn't track per-item macros upstream (see
     * reciplease-openapi-required-fields TODO / out-of-scope note on Recipe/InventoryItem).
     */
    public void logFood(final String userId, final String foodId, final String foodDisplayName,
                         final String mealType, final double amount, final LocalDate date) {
        final var connection = requireConnection(userId);
        final var dataPointId = UUID.randomUUID().toString();
        final var interval = new SessionTimeInterval(
                date.atStartOfDay(ZoneOffset.UTC).toInstant().toString(),
                date.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant().toString());

        final NutritionLogRequest body;
        if (foodId != null && !foodId.isBlank()) {
            // Best-effort: Google Health's documented Food resource name shape is
            // "food/{foodId}" — reused as the NutritionLog's food reference.
            body = new NutritionLogRequest(interval, mealType, "food/" + foodId, null, amount);
        } else {
            body = new NutritionLogRequest(interval, mealType, null, foodDisplayName, amount);
        }

        restClient.patch()
                .uri(NUTRITION_LOG_URL, dataPointId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + connection.accessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .toBodilessEntity();
    }

    /**
     * Returns {@code userId}'s connection. The frontend guarantees the access token is fresh
     * before ever calling into an endpoint backed by this method — it proactively refreshes
     * and pushes new tokens via {@link #storeConnection} before they expire — so there is no
     * expiry check or refresh call here.
     */
    private GoogleHealthConnection requireConnection(final String userId) {
        return googleHealthConnectionRepository.findByUserId(userId)
                .orElseThrow(() -> new GoogleHealthNotConnectedException(userId));
    }

    private record SessionTimeInterval(
            @JsonProperty("startTime") String startTime,
            @JsonProperty("endTime") String endTime) {}

    /**
     * Best-effort shape for a Google Health {@code NutritionLog} data point value. {@code
     * mealType} uses best-judgment enum naming (BREAKFAST/LUNCH/DINNER/SNACK) matching common
     * Google Fit/Health conventions — not confirmed against the live API.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private record NutritionLogRequest(
            @JsonProperty("interval") SessionTimeInterval interval,
            @JsonProperty("mealType") String mealType,
            @JsonProperty("food") String food,
            @JsonProperty("foodDisplayName") String foodDisplayName,
            @JsonProperty("amount") double amount) {}
}
