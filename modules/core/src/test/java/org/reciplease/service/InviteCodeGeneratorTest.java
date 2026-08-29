package org.reciplease.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.matchesPattern;

import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

class InviteCodeGeneratorTest {

    private final InviteCodeGenerator generator = new InviteCodeGenerator();

    @Test
    void generatesAlphanumericCodesOnly() {
        IntStream.range(0, 50)
                .mapToObj(i -> generator.generate())
                .forEach(code -> assertThat(code, matchesPattern("[A-Za-z0-9]{24}")));
    }

    @Test
    void generatesDistinctCodes() {
        var first = generator.generate();
        var second = generator.generate();

        assertThat(first.equals(second), org.hamcrest.Matchers.is(false));
    }
}
