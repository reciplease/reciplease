package org.reciplease.service;

import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.matchesPattern;

class ApiKeyGeneratorTest {

    private final ApiKeyGenerator generator = new ApiKeyGenerator();

    @Test
    void generatesKeysWithTheRcplPrefix() {
        IntStream.range(0, 50)
                .mapToObj(i -> generator.generate())
                .forEach(key -> assertThat(key, matchesPattern("rcpl_[A-Za-z0-9_-]{40}")));
    }

    @Test
    void generatesDistinctKeys() {
        var first = generator.generate();
        var second = generator.generate();

        assertThat(first.equals(second), is(false));
    }
}
