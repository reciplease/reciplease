package com.reciplease.repository.converter;

import org.junit.Before;
import org.junit.Test;

import java.sql.Date;
import java.time.LocalDate;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

public class LocalDateAttributeConverterTest {
    private LocalDateAttributeConverter localDateAttributeConverter;

    @Before
    public void setUp() {
        localDateAttributeConverter = new LocalDateAttributeConverter();
    }

    @Test
    public void shouldConvertLocalDate() {
        final LocalDate localDate = LocalDate.of(2019, 2, 1);
        final Date expected = Date.valueOf(localDate);

        final Date actual = localDateAttributeConverter.convertToDatabaseColumn(localDate);

        assertThat(actual, is(expected));
    }

    @Test
    public void shouldConvertNullLocalDate() {
        assertThat(localDateAttributeConverter.convertToDatabaseColumn(null), is(nullValue()));
    }

    @Test
    public void shouldConvertDate() {
        final LocalDate expected = LocalDate.of(2019, 2, 1);
        final Date date = Date.valueOf(expected);

        final LocalDate actual = localDateAttributeConverter.convertToEntityAttribute(date);

        assertThat(actual, is(expected));
    }

    @Test
    public void shouldConvertNullDate() {
        assertThat(localDateAttributeConverter.convertToEntityAttribute(null), is(nullValue()));
    }
}