package com.digitalbank.customerservice.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class OpenApiConfiguration {

    @Bean
    OpenAPI customerServiceOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Digital Bank Customer Service API")
                        .description("Customer profile and lifecycle APIs for the Digital Bank Java platform.")
                        .version("1.0.0"));
    }
}
