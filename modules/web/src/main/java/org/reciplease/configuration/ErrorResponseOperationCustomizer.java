package org.reciplease.configuration;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;

/**
 * Adds documented error {@code @ApiResponse}s ({@code 400}/{@code 401}/{@code 403}) to
 * operations based on what can actually make the underlying controller method fail, without
 * hand-annotating every endpoint and (unlike an explicit {@code @ApiResponses} annotation on the
 * method — see the AspectJ approach this replaced) without suppressing springdoc's automatic
 * inference of the operation's success response from the method's return type: this customizer
 * runs after springdoc has already built the operation (success response included), so it only
 * ever adds to {@link Operation#getResponses()}, never replaces it.
 * <p>
 * Attaches a plain {@code $ref} schema pointing at {@code ErrorResponse} to each added response;
 * {@link ErrorResponseSchemaRegistrar} is responsible for actually registering that schema into
 * {@code components.schemas} — {@link OperationCustomizer}s only see one {@link Operation} at a
 * time (not the {@code OpenAPI} document being assembled), so schema registration can't happen
 * here.
 */
@Component
public class ErrorResponseOperationCustomizer implements OperationCustomizer {

    static final String ERROR_RESPONSE_REF = "#/components/schemas/ErrorResponse";

    @Override
    public Operation customize(final Operation operation, final HandlerMethod handlerMethod) {
        final Method method = handlerMethod.getMethod();

        if (isValidated(method)) {
            addResponseIfAbsent(operation, "400", "Validation failed");
        }

        if (isProtected(method)) {
            addResponseIfAbsent(operation, "401", "Not authenticated");
            addResponseIfAbsent(operation, "403", "Not authorized");
        }

        return operation;
    }

    /** Any {@code @Valid} parameter, or any {@code @Pattern}-annotated parameter (e.g. a
     * {@code @PathVariable}/{@code @RequestParam}), can 400 on a Bean Validation failure. */
    private boolean isValidated(final Method method) {
        for (final Parameter parameter : method.getParameters()) {
            if (parameter.isAnnotationPresent(Valid.class) || parameter.isAnnotationPresent(Pattern.class)) {
                return true;
            }
        }
        return false;
    }

    /** {@code @PreAuthorize} (directly, or via the {@code @HouseMember}/{@code @HouseOwner}
     * meta-annotations) on the method or its declaring class can 401/403. */
    private boolean isProtected(final Method method) {
        return hasAnyOf(method, PreAuthorize.class, HouseMember.class, HouseOwner.class)
                || hasAnyOf(method.getDeclaringClass(), PreAuthorize.class, HouseMember.class, HouseOwner.class);
    }

    @SafeVarargs
    private boolean hasAnyOf(final AnnotatedElement element, final Class<? extends Annotation>... annotationTypes) {
        for (final Class<? extends Annotation> annotationType : annotationTypes) {
            if (element.isAnnotationPresent(annotationType)) {
                return true;
            }
        }
        return false;
    }

    private void addResponseIfAbsent(final Operation operation, final String statusCode, final String description) {
        final ApiResponses responses = ensureResponses(operation);
        if (responses.containsKey(statusCode)) {
            return;
        }
        responses.addApiResponse(statusCode, errorApiResponse(description));
    }

    private ApiResponses ensureResponses(final Operation operation) {
        if (operation.getResponses() == null) {
            operation.setResponses(new ApiResponses());
        }
        return operation.getResponses();
    }

    private ApiResponse errorApiResponse(final String description) {
        return new ApiResponse()
                .description(description)
                .content(new Content().addMediaType("*/*", new MediaType().schema(new Schema<>().$ref(ERROR_RESPONSE_REF))));
    }
}
