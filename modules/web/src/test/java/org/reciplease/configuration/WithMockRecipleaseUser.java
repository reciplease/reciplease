package org.reciplease.configuration;

import org.springframework.security.test.context.support.WithMockUser;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An allowlisted Reciplease user — shorthand for {@code @WithMockUser(authorities =
 * "ROLE_RECIPLEASE")} so tests don't need to remember the exact authority string. Does not imply
 * any house membership; stub the {@code HouseAccess} {@code @MockitoBean} separately for that
 * (see {@link WithHouseOwner}/{@link WithHouseMember} for the semantic names to reach for).
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@WithMockUser(authorities = "ROLE_RECIPLEASE")
public @interface WithMockRecipleaseUser {
}
