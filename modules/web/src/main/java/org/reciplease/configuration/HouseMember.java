package org.reciplease.configuration;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Requires an allowlisted user who is a member (OWNER or READ_ONLY) of the house
 * named by the {@code X-RCPLS-House-Id} header. Use on endpoints that only read
 * house-scoped data.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("hasRole('RECIPLEASE') and @houseAccess.isMember()")
@Parameter(
        name = HouseAccess.HOUSE_HEADER,
        in = ParameterIn.HEADER,
        required = true,
        description = "The house this request is scoped to.",
        schema = @Schema(type = "string"))
public @interface HouseMember {}
