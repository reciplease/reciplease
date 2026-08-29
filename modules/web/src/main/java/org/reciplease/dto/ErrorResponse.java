package org.reciplease.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

/**
 * Documentation-only stand-in for the error body Spring Boot's default error handling produces
 * (via {@code DefaultErrorAttributes}) on unhandled exceptions and Bean Validation failures, e.g.
 * {@code {"timestamp":"...","status":400,"error":"Bad Request","path":"/api/recipes"}}. Not the
 * class actually thrown/serialized in production — springdoc just needs a {@code @Schema}-visible
 * type to reference for {@code @ApiResponse} declarations describing that shape.
 */
@Schema(description = "Standard Spring Boot error body returned for unhandled request failures.")
public record ErrorResponse(Instant timestamp, int status, String error, String path) {}
