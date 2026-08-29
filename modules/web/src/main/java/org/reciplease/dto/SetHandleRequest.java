package org.reciplease.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

/** Body of {@code POST /api/me/handle}. */
public record SetHandleRequest(
        @Size(min = 1, max = 30) @Schema(minLength = 1, maxLength = 30)
        String handle) {}
