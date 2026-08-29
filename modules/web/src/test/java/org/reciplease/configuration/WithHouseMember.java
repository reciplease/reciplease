package org.reciplease.configuration;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Semantic alias for {@link WithMockRecipleaseUser}, used on tests that go on to stub the
 * {@code HouseAccess} {@code @MockitoBean} as a READ_ONLY member (e.g.
 * {@code when(houseAccess.isMember()).thenReturn(true)} with {@code isOwner()} left false) — see
 * {@link WithHouseOwner} for why the annotation itself can't do the stubbing.
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@WithMockRecipleaseUser
public @interface WithHouseMember {}
