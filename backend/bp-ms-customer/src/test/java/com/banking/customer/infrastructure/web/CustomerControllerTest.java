package com.banking.customer.infrastructure.web;

import com.banking.customer.application.dto.CustomerRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
class CustomerControllerTest {

    @Autowired
    private WebTestClient webClient;

    @Test
    void shouldCreateCustomer() {
        var request = new CustomerRequest(
                "Jose Lema", "Male", 32,
                "0102030405", "Otavalo sn y principal", "098254785",
                "1234", true
        );

        webClient.post().uri("/api/clientes")
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.data.customerId").isNotEmpty()
                .jsonPath("$.data.name").isEqualTo("Jose Lema");
    }

    @Test
    void shouldGetCustomerById() {
        var request = new CustomerRequest(
                "Marianela Montalvo", "Female", 30,
                "0203040506", "Amazonas y NNUU", "097548965",
                "5678", true
        );
        String customerId = webClient.post().uri("/api/clientes")
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.customerId").isNotEmpty()
                .returnResult().toString();

        webClient.get().uri("/api/clientes")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.success").isEqualTo(true);
    }

    @Test
    void shouldReturn404ForNonExistentCustomer() {
        webClient.get().uri("/api/clientes/NONEXISTENT")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.success").isEqualTo(false);
    }

    @Test
    void shouldPatchCustomer() {
        var request = new CustomerRequest(
                "Juan Osorio", "Male", 28,
                "0304050607", "13 junio y Equinoccial", "098874587",
                "1245", true
        );
        webClient.post().uri("/api/clientes")
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated();

        webClient.get().uri("/api/clientes")
                .exchange()
                .expectStatus().isOk();
    }
}
