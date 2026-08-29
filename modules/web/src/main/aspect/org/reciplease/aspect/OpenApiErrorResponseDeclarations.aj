package org.reciplease.aspect;

/**
 * Compile-time AspectJ {@code declare @method} declarations that attach documented error
 * {@code @ApiResponse}s to controller methods based on what can actually make them fail, without
 * hand-annotating every endpoint:
 * <ul>
 *   <li>any method with a {@code @jakarta.validation.Valid}-annotated parameter, or a
 *       {@code @jakarta.validation.constraints.Pattern}-annotated {@code @PathVariable}/
 *       {@code @RequestParam}, can 400 on a Bean Validation failure — see
 *       {@link org.reciplease.configuration.ValidationExceptionHandler}.</li>
 *   <li>any method carrying {@code @PreAuthorize} (directly, or via the {@code @HouseMember}/
 *       {@code @HouseOwner} meta-annotations — AspectJ's annotation-pattern matching does not see
 *       through meta-annotations, so all three are matched explicitly) can 401 (no/invalid
 *       credentials) or 403 (authenticated but not authorized) — see
 *       {@code MethodSecurityTestSupport.AuthorizationDeniedAdvice} for the real
 *       exception-to-status mapping this documents.</li>
 * </ul>
 * Woven onto the already-{@code javac}-compiled {@code .class} files at build time (binary
 * weaving via {@code -inpath}, not Spring AOP dynamic proxies — see the aspectj-maven-plugin
 * config in this module's pom.xml for why), so the annotations are present on the actual method
 * for springdoc's reflection-based scanning to see at application startup.
 * <p>
 * A single joinpoint can only receive one physical instance of a given annotation type from
 * {@code declare @method} (a second {@code declare} adding another {@code @ApiResponse} to an
 * already-annotated method is silently dropped, even though {@code @ApiResponse} is
 * {@code @Repeatable}) — so instead of one declare per status code, the three mutually exclusive
 * combinations a controller method can actually be in (400-only, 401/403-only, or all three) are
 * declared separately below, each attaching a single {@code @ApiResponses} container with
 * exactly the entries that combination needs.
 */
public aspect OpenApiErrorResponseDeclarations {

    declare @method:
        (* org.reciplease.controller..*.*(.., @jakarta.validation.Valid (*), ..) || * org.reciplease.controller..*.*(.., @jakarta.validation.constraints.Pattern (*), ..)) && !(@org.springframework.security.access.prepost.PreAuthorize * org.reciplease.controller..*.*(..) || @org.reciplease.configuration.HouseMember * org.reciplease.controller..*.*(..) || @org.reciplease.configuration.HouseOwner * org.reciplease.controller..*.*(..)):
        @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "Validation failed",
                content = @io.swagger.v3.oas.annotations.media.Content(
                    schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = org.reciplease.dto.ErrorResponse.class)))
        });

    declare @method:
        !(* org.reciplease.controller..*.*(.., @jakarta.validation.Valid (*), ..) || * org.reciplease.controller..*.*(.., @jakarta.validation.constraints.Pattern (*), ..)) && (@org.springframework.security.access.prepost.PreAuthorize * org.reciplease.controller..*.*(..) || @org.reciplease.configuration.HouseMember * org.reciplease.controller..*.*(..) || @org.reciplease.configuration.HouseOwner * org.reciplease.controller..*.*(..)):
        @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "401",
                description = "Not authenticated",
                content = @io.swagger.v3.oas.annotations.media.Content(
                    schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = org.reciplease.dto.ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "403",
                description = "Not authorized",
                content = @io.swagger.v3.oas.annotations.media.Content(
                    schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = org.reciplease.dto.ErrorResponse.class)))
        });

    declare @method:
        (* org.reciplease.controller..*.*(.., @jakarta.validation.Valid (*), ..) || * org.reciplease.controller..*.*(.., @jakarta.validation.constraints.Pattern (*), ..)) && (@org.springframework.security.access.prepost.PreAuthorize * org.reciplease.controller..*.*(..) || @org.reciplease.configuration.HouseMember * org.reciplease.controller..*.*(..) || @org.reciplease.configuration.HouseOwner * org.reciplease.controller..*.*(..)):
        @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "Validation failed",
                content = @io.swagger.v3.oas.annotations.media.Content(
                    schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = org.reciplease.dto.ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "401",
                description = "Not authenticated",
                content = @io.swagger.v3.oas.annotations.media.Content(
                    schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = org.reciplease.dto.ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "403",
                description = "Not authorized",
                content = @io.swagger.v3.oas.annotations.media.Content(
                    schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = org.reciplease.dto.ErrorResponse.class)))
        });
}
