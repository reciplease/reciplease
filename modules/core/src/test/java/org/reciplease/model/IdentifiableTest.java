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
        final UUID uuid = UUID.randomUUID();

        final TestModel model = TestModel.builder()
            .uuid(uuid)
            .build();

        assertThat(model.getUuid(), is(uuid));
    }
}
