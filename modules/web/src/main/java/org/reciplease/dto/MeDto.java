package org.reciplease.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/** Response of {@code GET /api/me} and {@code POST /api/me/handle}. */
@Schema(name = "Me")
public record MeDto(String id, @Schema(nullable = true) String handle) {}
