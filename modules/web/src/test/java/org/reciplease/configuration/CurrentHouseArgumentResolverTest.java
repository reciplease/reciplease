package org.reciplease.configuration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.context.request.NativeWebRequest;

@MockitoSettings
class CurrentHouseArgumentResolverTest {

    private final CurrentHouseArgumentResolver resolver = new CurrentHouseArgumentResolver();

    @Mock
    private NativeWebRequest webRequest;

    @Mock
    private MethodParameter parameter;

    @Test
    void resolvesTheHouseHeaderWhenPresent() throws Exception {
        when(webRequest.getHeader(HouseAccess.HOUSE_HEADER)).thenReturn("house-1");

        assertThat(resolver.resolveArgument(parameter, null, webRequest, null), is("house-1"));
    }

    @Test
    void throwsWhenTheHouseHeaderIsMissing() {
        when(webRequest.getHeader(HouseAccess.HOUSE_HEADER)).thenReturn(null);

        assertThrows(
                MissingRequestHeaderException.class, () -> resolver.resolveArgument(parameter, null, webRequest, null));
    }

    @Test
    void supportsAStringParameterAnnotatedWithCurrentHouse() throws Exception {
        final var stringParameter =
                new MethodParameter(getClass().getDeclaredMethod("withCurrentHouseString", String.class), 0);
        assertThat(resolver.supportsParameter(stringParameter), is(true));
    }

    @Test
    void doesNotSupportAnUnannotatedStringParameter() throws Exception {
        final var stringParameter =
                new MethodParameter(getClass().getDeclaredMethod("withoutAnnotation", String.class), 0);
        assertThat(resolver.supportsParameter(stringParameter), is(false));
    }

    private void withCurrentHouseString(@CurrentHouse final String houseId) {}

    private void withoutAnnotation(final String houseId) {}
}
