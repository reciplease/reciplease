package org.reciplease.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.reciplease.model.FoodConsumption;
import org.reciplease.model.GoogleHealthConnection;
import org.reciplease.model.LoggedFoodHistoryEntry;
import org.reciplease.repository.GoogleHealthConnectionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Clock;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Talks to the Google Health API (nutrition logging, nutrition-log history) on behalf of a
 * Reciplease user, and persists tokens pushed to it by the frontend via {@link
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
 * #storeConnection(String, String, String, long, String)} to be persisted.
 * <p>
 * Implements core's {@link FoodConsumptionLoggerPort} — this is the only adapter for that port
 * in the codebase today. Google Health's own {@code food} catalog search (exact-match only, no
 * barcode) is deliberately not exposed here; that gap is filled by {@code FoodCatalogPort}'s
 * Open Food Facts implementation instead, composed together in core's {@code FoodSearchService}.
 */
@Service
public class GoogleHealthAdapter implements FoodConsumptionLoggerPort {

    private static final String NUTRITION_LOG_LIST_URL = "https://health.googleapis.com/v4/users/me/dataTypes/nutrition-log/dataPoints";
    private static final String NUTRITION_LOG_URL = "https://health.googleapis.com/v4/users/me/dataTypes/nutrition-log/dataPoints/{dataPointId}";

    private final GoogleHealthConnectionRepository googleHealthConnectionRepository;
    private final Clock clock;
    private final RestClient restClient;

    @Autowired
    public GoogleHealthAdapter(final GoogleHealthConnectionRepository googleHealthConnectionRepository,
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
    GoogleHealthAdapter(final GoogleHealthConnectionRepository googleHealthConnectionRepository,
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

    @Override
    public boolean isConnected(final String userId) {
        return googleHealthConnectionRepository.findByUserId(userId).isPresent();
    }

    /**
     * Lists this user's own {@code nutrition-log} data points, most-recent-first, mapping each
     * to a {@link LoggedFoodHistoryEntry} for fuzzy-matching against a new search query.
     * {@code consumptionId} carries the referenced {@code Food} resource's id (the {@code
     * food/{foodId}} reference, stripped of its prefix) when the historic entry was logged
     * against an identified food — {@code null} for entries logged anonymously by name only,
     * since there's no food to re-reference.
     * <p>
     * Best-effort: this is a new Google Health API call not previously made anywhere in this
     * codebase (unlike the PATCH-to-log call below, which is confirmed against live 400
     * responses per its own history), so the exact list response shape needs live-API
     * verification before this ships.
     */
    @Override
    public List<LoggedFoodHistoryEntry> history(final String userId) {
        final var connection = requireConnection(userId);
        final var response = restClient.get()
                .uri(NUTRITION_LOG_LIST_URL)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + connection.accessToken())
                .retrieve()
                .body(NutritionLogListResponse.class);

        if (response == null || response.dataPoint() == null) {
            return List.of();
        }
        return response.dataPoint().stream()
                .map(dataPoint -> new LoggedFoodHistoryEntry(foodIdFrom(dataPoint.food()), dataPoint.foodDisplayName()))
                .toList();
    }

    private static String foodIdFrom(final String foodReference) {
        return foodReference != null && foodReference.startsWith("food/") ? foodReference.substring("food/".length()) : null;
    }

    /**
     * Logs a food-eaten entry as a {@code NutritionLog} data point against {@code userId}'s
     * Google Health nutrition log. The data point id is client-generated (a fresh UUID per
     * call, per the Google Health API's PATCH-to-create-or-update contract).
     * <p>
     * When {@code consumption.identifiedFoodId()} is present (the primary path — matches the UI
     * flow of picking a history result), the data point references that food and Google
     * populates its nutrients/energy itself. Otherwise this logs an "anonymous food" entry using
     * just {@code displayName}, optionally with manually supplied {@code nutrients} when the
     * consumer picked a food-catalog (e.g. Open Food Facts) search/barcode result.
     */
    @Override
    public void log(final FoodConsumption consumption) {
        final var connection = requireConnection(consumption.userId());
        final var dataPointId = UUID.randomUUID().toString();
        final var date = consumption.date();
        final var interval = new SessionTimeInterval(
                date.atStartOfDay(ZoneOffset.UTC).toInstant().toString(),
                date.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant().toString());

        final var mealType = consumption.mealType().name();
        final var nutrients = consumption.nutrients().map(GoogleHealthAdapter::toNutrientsPayload).orElse(null);

        final NutritionLogRequest body;
        if (consumption.identifiedFoodId().isPresent()) {
            // Best-effort: Google Health's documented Food resource name shape is
            // "food/{foodId}" — reused as the NutritionLog's food reference.
            body = new NutritionLogRequest(interval, mealType, "food/" + consumption.identifiedFoodId().get(), null, consumption.amount(), null);
        } else {
            body = new NutritionLogRequest(interval, mealType, null, consumption.displayName(), consumption.amount(), nutrients);
        }

        restClient.patch()
                .uri(NUTRITION_LOG_URL, dataPointId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + connection.accessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .toBodilessEntity();
    }

    private static NutrientsPayload toNutrientsPayload(final org.reciplease.model.Nutrients nutrients) {
        return new NutrientsPayload(nutrients.energyKcal(), nutrients.proteinG(), nutrients.fatG(), nutrients.carbohydrateG());
    }

    /**
     * Returns {@code userId}'s connection. The frontend guarantees the access token is fresh
     * before ever calling into an endpoint backed by this method — it proactively refreshes
     * and pushes new tokens via {@link #storeConnection} before they expire — so there is no
     * expiry check or refresh call here.
     */
    private GoogleHealthConnection requireConnection(final String userId) {
        return googleHealthConnectionRepository.findByUserId(userId)
                .orElseThrow(() -> new FoodConsumptionLoggerNotConnectedException(userId));
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
            @JsonProperty("amount") double amount,
            @JsonProperty("nutrients") NutrientsPayload nutrients) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private record NutrientsPayload(
            @JsonProperty("energy") Double energyKcal,
            @JsonProperty("protein") Double proteinG,
            @JsonProperty("totalFat") Double fatG,
            @JsonProperty("totalCarbohydrate") Double carbohydrateG) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record NutritionLogListResponse(@JsonProperty("dataPoint") List<NutritionLogDataPoint> dataPoint) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record NutritionLogDataPoint(
            @JsonProperty("food") String food,
            @JsonProperty("foodDisplayName") String foodDisplayName) {}
}
