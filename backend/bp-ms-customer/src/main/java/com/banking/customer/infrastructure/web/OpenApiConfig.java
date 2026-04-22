package com.banking.customer.infrastructure.web;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customerOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Customer Microservice API")
                        .description("Gestión de clientes (Persona + Cliente). CRUD completo con soft-delete.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Juan Carlos De La Cruz Perez")
                                .url("https://devsu.com")))
                .servers(List.of(
                        new Server().url("http://localhost:8081").description("Local")));
    }
}

