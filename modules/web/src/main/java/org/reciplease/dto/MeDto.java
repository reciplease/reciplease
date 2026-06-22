package org.reciplease.dto;

/** Response of {@code GET /api/me} and {@code POST /api/me/handle}. */
public record MeDto(String id, String handle) {}
