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
        return new OpenAPI()
                .info(new Info().title("Reciplease API")
                        .description("Recipe management and more!")
                        .version(version));
    }
}
