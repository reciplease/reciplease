package org.reciplease.model;

import lombok.experimental.SuperBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class IdentifiableTest {

    @SuperBuilder
    static class TestModel extends Identifiable {
    }

    @Test
    @DisplayName("should set id through builder")
    void setId() {
        final String id = "5f8d04b3d3b9a72b8c7e1a4f";

        final TestModel model = TestModel.builder()
                .id(id)
                .build();

        assertThat(model.getId(), is(id));
    }
}
