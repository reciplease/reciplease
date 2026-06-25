package org.reciplease.dto;

import java.util.List;

/** Response of {@code GET /api/me/identities}. Provider ids are never exposed. */
public record IdentitiesDto(List<LinkedIdentityDto> identities) {}
