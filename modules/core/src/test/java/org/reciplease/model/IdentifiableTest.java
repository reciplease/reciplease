package org.reciplease.model;

import lombok.experimental.SuperBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class IdentifiableTest {

    @SuperBuilder
    static class TestModel extends Identifiable {
    }

    @Test
    @DisplayName("should set UUID through builder")
    void setUuid() {
        final UUID randomId = UUID.randomUUID();

        final TestModel model = TestModel.builder()
                .id(randomId)
                .build();

        assertThat(model.getId(), is(randomId));
    }
}
