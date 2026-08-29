package org.reciplease.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.reciplease.dto.GoogleHealthConnectionStatusDto;
import org.reciplease.dto.GoogleHealthTokensRequest;
import org.reciplease.dto.LogGoogleHealthFoodRequest;
import org.reciplease.model.FoodConsumption;
import org.reciplease.model.GoogleHealthConnection;
import org.reciplease.model.MealType;
import org.reciplease.model.Nutrients;
import org.reciplease.service.FoodConsumptionLoggerNotConnectedException;
import org.reciplease.service.FoodConsumptionLoggerPort;
import org.reciplease.service.GoogleHealthAdapter;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Links/unlinks the current user's Google Health account, and logs food consumption to it. The
 * Google OAuth2 authorize URL, the authorization-code exchange, and all token refreshes are
 * handled entirely by the Next.js frontend (which already holds the Google OAuth client
 * id/secret for NextAuth sign-in) — this controller never talks to Google's OAuth endpoints and
 * never holds the client secret. The frontend pushes the resulting tokens here via {@code PUT
 * /connection} for storage, both for the initial link and for every subsequent refresh.
 * <p>
 * Connection management (this controller's own concern) depends directly on the concrete {@link
 * GoogleHealthAdapter}, since there's only ever one consumption-logging provider today and no
 * polymorphism to gain by abstracting link/unlink. Logging depends only on core's {@link
 * FoodConsumptionLoggerPort} — see {@code FoodSearchController} for the search side.
 */
@RestController
@RequestMapping("api/google-health")
@Tag(name = "Google Health")
@RequiredArgsConstructor
public class GoogleHealthController {

    private final GoogleHealthAdapter googleHealthAdapter;
    private final FoodConsumptionLoggerPort foodConsumptionLoggerPort;

    @GetMapping("connection")
    @PreAuthorize("isAuthenticated()")
    @Operation(operationId = "findGoogleHealthConnection")
    public GoogleHealthConnectionStatusDto connection() {
        return toDto(googleHealthAdapter.connectionStatus(currentUserId()));
    }

    @PutMapping("connection")
    @PreAuthorize("isAuthenticated()")
    @Operation(operationId = "updateGoogleHealthConnection")
    public ResponseEntity<GoogleHealthConnectionStatusDto> putConnection(
            @RequestBody final GoogleHealthTokensRequest request) {
        final var connection = googleHealthAdapter.storeConnection(
                currentUserId(), request.accessToken(), request.refreshToken(), request.expiresIn(), request.scope());
        return ResponseEntity.ok(toDto(Optional.of(connection)));
    }

    @DeleteMapping("connection")
    @PreAuthorize("isAuthenticated()")
    @Operation(operationId = "disconnectGoogleHealth")
    public ResponseEntity<Void> disconnect() {
        googleHealthAdapter.disconnect(currentUserId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("foods/log")
    @PreAuthorize("isAuthenticated()")
    @Operation(operationId = "logGoogleHealthFood")
    public ResponseEntity<Void> logFood(@RequestBody final LogGoogleHealthFoodRequest request) {
        try {
            foodConsumptionLoggerPort.log(toFoodConsumption(currentUserId(), request));
            return ResponseEntity.ok().build();
        } catch (final FoodConsumptionLoggerNotConnectedException e) {
            return ResponseEntity.notFound().build();
        }
    }

    private static FoodConsumption toFoodConsumption(final String userId, final LogGoogleHealthFoodRequest request) {
        final var nutrients = Optional.ofNullable(request.nutrients())
                .map(n -> new Nutrients(n.energyKcal(), n.proteinG(), n.fatG(), n.carbohydrateG()));
        return new FoodConsumption(
                userId,
                Optional.ofNullable(request.foodId()),
                request.foodDisplayName(),
                nutrients,
                MealType.valueOf(request.mealType()),
                request.amount(),
                request.date());
    }

    private static GoogleHealthConnectionStatusDto toDto(final Optional<GoogleHealthConnection> connection) {
        return connection
                .map(c -> new GoogleHealthConnectionStatusDto(true, c.expiresAt(), c.refreshToken()))
                .orElseGet(GoogleHealthConnectionStatusDto::disconnected);
    }

    private String currentUserId() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
