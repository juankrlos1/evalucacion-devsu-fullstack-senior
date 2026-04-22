package com.banking.customer;

import com.intuit.karate.junit5.Karate;

class KarateIntegrationTest {

    @Karate.Test
    Karate testClientes() {
        return Karate.run("classpath:features/clientes.feature").relativeTo(getClass());
    }
}

