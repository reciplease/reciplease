package org.reciplease.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;

/** A single linked sign-in provider, as exposed to its own owner. */
@Schema(name = "LinkedIdentity")
public record LinkedIdentityDto(
        @Schema(requiredMode = REQUIRED) String provider,
        @Schema(nullable = true) String email) {}
