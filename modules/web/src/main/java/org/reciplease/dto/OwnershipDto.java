package org.reciplease.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "Ownership")
public record OwnershipDto(
        @Schema(nullable = true) UserSummaryDto createdBy,
        @Schema(nullable = true) UserSummaryDto updatedBy) {}
