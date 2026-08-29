package org.reciplease.configuration;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Requires an allowlisted user who is an OWNER of the house named by the
 * {@code X-RCPLS-House-Id} header. Use on endpoints that create/edit/delete
 * house-scoped data.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("hasRole('RECIPLEASE') and @houseAccess.isOwner()")
public @interface HouseOwner {}
