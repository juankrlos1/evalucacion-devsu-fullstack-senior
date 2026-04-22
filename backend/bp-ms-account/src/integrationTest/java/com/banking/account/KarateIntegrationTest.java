package com.banking.account;

import com.intuit.karate.junit5.Karate;

class KarateIntegrationTest {

    @Karate.Test
    Karate testCuentas() {
        return Karate.run("classpath:features/cuentas.feature").relativeTo(getClass());
    }

    @Karate.Test
    Karate testMovimientos() {
        return Karate.run("classpath:features/movimientos.feature").relativeTo(getClass());
    }

    @Karate.Test
    Karate testReportes() {
        return Karate.run("classpath:features/reportes.feature").relativeTo(getClass());
    }
}

