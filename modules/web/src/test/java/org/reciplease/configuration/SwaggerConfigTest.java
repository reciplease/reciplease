package org.reciplease.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@SpringBootTest(classes = SwaggerConfig.class, properties = "reciplease.version=1.2.3")
class SwaggerConfigTest {
    @Autowired
    private OpenAPI openAPI;

    @Test
    void version() {
        assertThat(openAPI.getInfo().getVersion(), is("1.2.3"));
    }
}