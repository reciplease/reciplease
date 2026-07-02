package org.reciplease.controller;

import lombok.RequiredArgsConstructor;
import org.reciplease.dto.FitbitCallbackRequest;
import org.reciplease.dto.FitbitConnectionStatusDto;
import org.reciplease.dto.LogFoodRequest;
import org.reciplease.service.FitbitNotConnectedException;
import org.reciplease.service.FitbitService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Links/unlinks the current user's Fitbit account and proxies Fitbit's food search/logging
 * endpoints on their behalf. Fitbit's OAuth2 authorize URL is built by the frontend directly
 * (see {@link FitbitService}); this controller only handles the token exchange and the
 * authenticated calls that need the resulting access token.
 */
@RestController
@RequestMapping("api/fitbit")
@RequiredArgsConstructor
public class FitbitController {

    private final FitbitService fitbitService;

    @GetMapping("connection")
    @PreAuthorize("isAuthenticated()")
    public FitbitConnectionStatusDto connection() {
        return new FitbitConnectionStatusDto(fitbitService.connectionStatus(currentUserId()).isPresent());
    }

    @PostMapping("callback")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<FitbitConnectionStatusDto> callback(@RequestBody final FitbitCallbackRequest request) {
        fitbitService.exchangeCode(currentUserId(), request.code(), request.codeVerifier());
        return ResponseEntity.ok(new FitbitConnectionStatusDto(true));
    }

    @DeleteMapping("connection")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> disconnect() {
        fitbitService.disconnect(currentUserId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("foods/search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> searchFoods(@RequestParam final String query) {
        try {
            final var results = fitbitService.searchFoods(currentUserId(), query);
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(results);
        } catch (final FitbitNotConnectedException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("foods/log")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> logFood(@RequestBody final LogFoodRequest request) {
        try {
            fitbitService.logFood(currentUserId(), request.foodId(), request.unitId(), request.amount(), request.mealTypeId(), request.date());
            return ResponseEntity.ok().build();
        } catch (final FitbitNotConnectedException e) {
            return ResponseEntity.notFound().build();
        }
    }

    private String currentUserId() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
