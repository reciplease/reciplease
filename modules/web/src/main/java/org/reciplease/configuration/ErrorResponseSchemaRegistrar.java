package org.reciplease.configuration;

import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.core.converter.ResolvedSchema;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import org.reciplease.dto.ErrorResponse;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springframework.stereotype.Component;

/**
 * Registers {@link ErrorResponse} into the final {@code components.schemas}, once, unconditionally
 * (a schema present but unreferenced is harmless, and it's always referenced whenever {@link
 * ErrorResponseOperationCustomizer} has added a {@code 400}/{@code 401}/{@code 403} response).
 * <p>
 * Runs as a {@link GlobalOpenApiCustomizer} rather than from within {@link
 * ErrorResponseOperationCustomizer} directly: an {@code OperationCustomizer} only sees one {@code
 * Operation} at a time, not the {@code OpenAPI} document being assembled, and springdoc does not
 * itself walk {@code $ref}s added by an {@code OperationCustomizer} to auto-register their target
 * schemas (unlike {@code $ref}s it discovers via a method's own return type/{@code @RequestBody},
 * from ordinary reflection-based scanning) — a {@code GlobalOpenApiCustomizer}, by contrast, runs
 * last and receives the actual {@link OpenAPI} instance springdoc renders, so registering directly
 * into it here is guaranteed to survive into the final spec.
 */
@Component
public class ErrorResponseSchemaRegistrar implements GlobalOpenApiCustomizer {

    @Override
    public void customise(final OpenAPI openApi) {
        final ResolvedSchema resolvedSchema = ModelConverters.getInstance()
                .resolveAsResolvedSchema(new AnnotatedType(ErrorResponse.class).resolveAsRef(true));

        if (openApi.getComponents() == null) {
            openApi.setComponents(new Components());
        }
        resolvedSchema.referencedSchemas.forEach((name, schema) -> {
            if (openApi.getComponents().getSchemas() == null
                    || !openApi.getComponents().getSchemas().containsKey(name)) {
                openApi.getComponents().addSchemas(name, schema);
            }
        });
    }
}
