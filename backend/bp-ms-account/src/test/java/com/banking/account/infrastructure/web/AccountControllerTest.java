package com.banking.account.infrastructure.web;

import com.banking.account.application.dto.AccountRequest;
import com.banking.account.application.dto.MovementRequest;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.math.BigDecimal;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AccountControllerTest {

    @Autowired
    private WebTestClient webClient;

    private static String createdAccountNumber;

    @Test
    @Order(1)
    void shouldCreateAccount() {
        var request = new AccountRequest("Savings", new BigDecimal("2000"), true, "CLI-001");

        var body = webClient.post().uri("/api/cuentas")
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.data.accountNumber").isNotEmpty()
                .jsonPath("$.data.availableBalance").isEqualTo(2000)
                .returnResult()
                .getResponseBody();

        var json = new String(body);
        createdAccountNumber = json.replaceAll(".*\"accountNumber\":\"([^\"]+)\".*", "$1");
    }

    @Test
    @Order(2)
    void shouldGetAccount() {
        webClient.get().uri("/api/cuentas/{num}", createdAccountNumber)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.data.accountNumber").isEqualTo(createdAccountNumber)
                .jsonPath("$.data.accountType").isEqualTo("Savings");
    }

    @Test
    @Order(3)
    void shouldReturn404ForNonExistentAccount() {
        webClient.get().uri("/api/cuentas/999999")
                .exchange()
                .expectStatus().isNotFound();
    }
}
