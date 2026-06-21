package org.reciplease.configuration;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Shared {@code @WebMvcTest} support for exercising {@code @PreAuthorize}/{@code @HouseOwner}/
 * {@code @HouseMember} the same way production does.
 * <p>
 * {@code @EnableMethodSecurity} alone makes the annotations active, but a denied check throws
 * {@code AuthorizationDeniedException} straight through MockMvc as an unhandled exception rather
 * than a 403 — in production that's caught by {@code ExceptionTranslationFilter} inside the real
 * {@code SecurityFilterChain}, which {@code @WebMvcTest} slices don't have. Standing up a full
 * filter chain just for that translation conflicts with how {@code @WithMockUser} populates the
 * security context in a slice test, so this translates the exception directly instead.
 * <p>
 * {@code @Import(MethodSecurityTestSupport.class)} once per {@code @WebMvcTest} class is enough.
 */
@TestConfiguration
@EnableMethodSecurity
public class MethodSecurityTestSupport {

    @RestControllerAdvice
    static class AuthorizationDeniedAdvice {
        @ExceptionHandler(AuthorizationDeniedException.class)
        ResponseEntity<Void> handle() {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }
}
