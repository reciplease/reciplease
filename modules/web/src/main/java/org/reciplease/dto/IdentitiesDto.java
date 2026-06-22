package org.reciplease.dto;

import java.util.List;

/** Response of {@code GET /api/me/identities}. Only provider names are exposed, never provider ids. */
public record IdentitiesDto(List<String> providers) {}
