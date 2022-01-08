package org.reciplease.model;

import lombok.experimental.SuperBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

class BaseModelTest {

    @SuperBuilder
    static class TestModel extends BaseModel {
    }

    @Test
    @DisplayName("can be built with random UUID")
    void withRandomUuid() {
        final var model = TestModel.builder()
                .randomUUID()
                .build();

        assertThat(model.getUuid(), is(not(nullValue())));
    }

}
