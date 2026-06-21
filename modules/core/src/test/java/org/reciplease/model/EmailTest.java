package org.reciplease.model;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class EmailTest {

    @Test
    void masksTheMiddleOfALongLocalPart() {
        assertThat(new Email("rhys.saldanha@gmail.com").masked(), is(new Email("rh**ha@gmail.com")));
    }

    @Test
    void masksAFiveCharacterLocalPart() {
        assertThat(new Email("owner@example.com").masked(), is(new Email("ow**er@example.com")));
    }

    @Test
    void keepsOnlyTheFirstCharacterForAShortLocalPart() {
        assertThat(new Email("abc@example.com").masked(), is(new Email("a**@example.com")));
    }

    @Test
    void fullyMasksAVeryShortLocalPart() {
        assertThat(new Email("ab@example.com").masked(), is(new Email("**@example.com")));
    }

    @Test
    void leavesTheDomainUntouched() {
        assertThat(new Email("rhys.saldanha@gmail.com").masked().value(), is("rh**ha@gmail.com"));
    }
}
