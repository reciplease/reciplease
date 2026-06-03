package org.reciplease.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI springShopOpenAPI(@Value("${reciplease.version}") final String version) {
        // No explicit server: SpringDoc derives it from the (possibly forwarded)
        // request, so "Try it out" targets the right base both directly and when
        // reverse-proxied under a prefix such as /swagger by the web app.
        return new OpenAPI()
                .info(new Info().title("Reciplease API")
                        .description("Recipe management and more!")
                        .version(version));
    }
}
