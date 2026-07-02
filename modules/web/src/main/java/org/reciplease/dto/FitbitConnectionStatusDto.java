package org.reciplease.dto;

/** Response of {@code GET /api/fitbit/connection}. */
public record FitbitConnectionStatusDto(boolean connected) {}
