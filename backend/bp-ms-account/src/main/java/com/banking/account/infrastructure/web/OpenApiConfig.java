package com.banking.account.infrastructure.web;

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
    public OpenAPI accountOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Account Microservice API")
                        .description("Gestión de cuentas, movimientos y reportes. "
                                + "Reglas: retiros negativos, depósitos positivos, "
                                + "límite diario $1000, reporte JSON/PDF.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Juan Carlos De La Cruz Perez")
                                .url("https://devsu.com")))
                .servers(List.of(
                        new Server().url("http://localhost:8082").description("Local")));
    }
}

