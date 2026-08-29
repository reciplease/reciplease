package org.reciplease.configuration;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Semantic alias for {@link WithMockRecipleaseUser}, used on tests that go on to stub the
 * {@code HouseAccess} {@code @MockitoBean} as an OWNER (e.g. {@code when(houseAccess.isOwner())
 * .thenReturn(true)}) — the annotation itself only grants {@code ROLE_RECIPLEASE}; it doesn't
 * stub house membership, since {@code @WithMockUser}-style annotations only build a
 * {@code SecurityContext} and have no reach into other beans.
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@WithMockRecipleaseUser
public @interface WithHouseOwner {}
