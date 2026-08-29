package org.reciplease.configuration;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Documents in the OpenAPI spec that this endpoint requires the
 * {@code X-RCPLS-House-Id} header — apply to any controller method gated by
 * {@code @PreAuthorize("@houseAccess.isMember()")} or {@code isOwner()}. Purely
 * documentation: {@link HouseAccess} still resolves the header off the raw
 * servlet request at runtime, this doesn't change that.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Parameter(
        name = HouseAccess.HOUSE_HEADER,
        in = ParameterIn.HEADER,
        required = true,
        description = "The house this request is scoped to.")
public @interface RequiresHouseHeader {}
