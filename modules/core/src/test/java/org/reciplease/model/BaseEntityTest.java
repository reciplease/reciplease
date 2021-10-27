package org.reciplease.model;

import lombok.experimental.SuperBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

class BaseEntityTest {

    @Test
    @DisplayName("can be built with random UUID")
    void withRandomUuid() {
        final var entity = SomeEntity.builder()
                .randomUUID()
                .build();

        assertThat(entity.getUuid(), is(not(nullValue())));
    }

    @SuperBuilder
    static class SomeEntity extends BaseEntity {
    }
}