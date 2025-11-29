package com.gerze.order_service.infrastructure.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI orderServiceOpenApi() {
        return new OpenAPI()
            .info(new Info()
                .title("Order Service API")
                .description("Reactive API for managing orders, products, and statuses.")
                .version("v1")
                .license(new License()
                    .name("Apache 2.0")
                    .url("https://www.apache.org/licenses/LICENSE-2.0"))
                .contact(new Contact().name("Order Service Team")));
    }

    @Bean
    public GroupedOpenApi orderServiceApi() {
        return GroupedOpenApi.builder()
            .group("order-service")
            .packagesToScan("com.gerze.order_service.infrastructure.rest")
            .pathsToMatch("/orders/**", "/products/**")
            .build();
    }
}
