package org.project.carsharingapp.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    private static final String TITLE = "Car Sharing App API";
    private static final String VERSION = "1.0";
    private static final String DESCRIPTION = "Documentation API";
    private static final String SECURITY_SCHEME_NAME = "bearerAuth";
    private static final String SCHEME = "bearer";
    private static final String BEARER_FORMAT = "JWT";

    @Bean
    public OpenAPI api() {
        return new OpenAPI()
                .info(new Info()
                        .title(TITLE)
                        .version(VERSION)
                        .description(DESCRIPTION))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme(SCHEME)
                                        .bearerFormat(BEARER_FORMAT)))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME));
    }
}
