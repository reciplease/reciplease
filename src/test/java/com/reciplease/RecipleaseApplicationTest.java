package com.reciplease;

import org.junit.Ignore;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@Ignore
public class RecipleaseApplicationTest {

    @Test
    public void shouldRunMainMethod() {
        RecipleaseApplication.main(new String[]{});
        assertThat(true, is(true));
    }

}