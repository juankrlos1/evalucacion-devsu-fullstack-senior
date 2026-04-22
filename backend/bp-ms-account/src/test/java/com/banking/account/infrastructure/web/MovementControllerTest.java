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
class MovementControllerTest {

    @Autowired
    private WebTestClient webClient;

    private static String testAccountNumber;

    @Test
    @Order(1)
    void setup() {
        // Create test account (account number is auto-generated)
        var account = new AccountRequest("Savings", new BigDecimal("2000"), true, "CLI-001");
        var body = webClient.post().uri("/api/cuentas")
                .bodyValue(account)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .returnResult()
                .getResponseBody();

        var json = new String(body);
        testAccountNumber = json.replaceAll(".*\"accountNumber\":\"([^\"]+)\".*", "$1");
    }

    @Test
    @Order(2)
    void shouldCreateMovement() {
        var request = new MovementRequest(testAccountNumber, new BigDecimal("-575"));

        webClient.post().uri("/api/movimientos")
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.data.movementType").isEqualTo("Withdrawal")
                .jsonPath("$.data.balance").isEqualTo(1425);
    }

    @Test
    @Order(3)
    void shouldRejectInsufficientBalance() {
        var request = new MovementRequest(testAccountNumber, new BigDecimal("-9999"));

        webClient.post().uri("/api/movimientos")
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.success").isEqualTo(false)
                .jsonPath("$.message").isEqualTo("Saldo no disponible");
    }

    @Test
    @Order(4)
    void shouldCreateDeposit() {
        var request = new MovementRequest(testAccountNumber, new BigDecimal("100"));

        webClient.post().uri("/api/movimientos")
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.data.movementType").isEqualTo("Deposit")
                .jsonPath("$.data.balance").isEqualTo(1525);
    }
}
