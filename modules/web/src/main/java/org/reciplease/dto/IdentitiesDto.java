package org.reciplease.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/** Response of {@code GET /api/me/identities}. Provider ids are never exposed. */
@Schema(name = "Identities")
public record IdentitiesDto(@Schema(requiredMode = REQUIRED) List<LinkedIdentityDto> identities) {}
