package org.reciplease.configuration;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Requires an allowlisted user who is an OWNER of the house named by the
 * {@code X-RCPLS-House-Id} header. Use on endpoints that create/edit/delete
 * house-scoped data.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("hasRole('RECIPLEASE') and @houseAccess.isOwner()")
public @interface HouseOwner {
}
