package com.examseating.anticheating.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Smart Examination Anti-Cheating & Seating Optimization API")
                        .version("1.0.0")
                        .description("REST API for intelligent exam seat allocation using Graph Coloring and Greedy Algorithms")
                        .contact(new Contact()
                                .name("Exam Seating System")
                                .url("http://localhost:8080")));
    }
}