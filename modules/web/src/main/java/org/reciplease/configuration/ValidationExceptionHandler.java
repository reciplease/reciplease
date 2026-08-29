package org.reciplease.configuration;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * {@code @Valid}/{@code @Validated} on a {@code @RequestBody} is enforced by Spring MVC's own
 * argument resolution and already 400s on failure without any help from here. But constraints on
 * a {@code @PathVariable} or {@code @RequestParam} (e.g. {@code InviteController}'s invite code
 * pattern) are enforced by the method-validation AOP interceptor instead, which throws a plain
 * {@link ConstraintViolationException} — with no handler for it, that surfaces as an unhandled
 * 500 rather than the 400 a caller-input validation failure should be.
 */
@RestControllerAdvice
public class ValidationExceptionHandler {

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Void> handleConstraintViolation(final ConstraintViolationException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
}
