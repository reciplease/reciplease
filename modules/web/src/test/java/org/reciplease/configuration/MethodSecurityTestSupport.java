package org.reciplease.configuration;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Shared {@code @WebMvcTest} support for exercising {@code @PreAuthorize}/{@code @HouseOwner}/
 * {@code @HouseMember} the same way production does.
 * <p>
 * {@code @EnableMethodSecurity} alone makes the annotations active, but a denied check throws
 * {@code AuthorizationDeniedException} (an authenticated caller lacking the required role) or
 * {@code AuthenticationCredentialsNotFoundException} (no caller at all) straight through MockMvc
 * as an unhandled exception rather than a 403/401 — in production those are caught by
 * {@code ExceptionTranslationFilter} inside the real {@code SecurityFilterChain}, which
 * {@code @WebMvcTest} slices don't have. Standing up a full filter chain just for that
 * translation conflicts with how {@code @WithMockUser} populates the security context in a slice
 * test, so this translates the exceptions directly instead.
 * <p>
 * {@code @Import(MethodSecurityTestSupport.class)} once per {@code @WebMvcTest} class is enough.
 */
@TestConfiguration
@EnableMethodSecurity
public class MethodSecurityTestSupport {

    @RestControllerAdvice
    static class AuthorizationDeniedAdvice {
        @ExceptionHandler(AuthorizationDeniedException.class)
        ResponseEntity<Void> handleDenied() {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        @ExceptionHandler(AuthenticationCredentialsNotFoundException.class)
        ResponseEntity<Void> handleNoCredentials() {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
