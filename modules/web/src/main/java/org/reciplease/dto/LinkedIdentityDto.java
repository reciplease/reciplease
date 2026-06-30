package org.reciplease.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/** A single linked sign-in provider, as exposed to its own owner. */
@Schema(name = "LinkedIdentity")
public record LinkedIdentityDto(String provider, @Schema(nullable = true) String email) {}
