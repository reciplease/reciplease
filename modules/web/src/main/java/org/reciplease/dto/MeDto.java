package org.reciplease.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;

/** Response of {@code GET /api/me} and {@code POST /api/me/handle}. */
@Schema(name = "Me")
public record MeDto(
        @Schema(requiredMode = REQUIRED) String id,
        @Schema(nullable = true) String handle) {}
