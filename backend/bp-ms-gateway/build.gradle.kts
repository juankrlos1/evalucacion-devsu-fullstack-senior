/* Spring Cloud Gateway - Reactive API Gateway
 This module overrides root subproject defaults since it doesn't need JPA/MapStruct */

// Spring Cloud BOM
dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:2023.0.3")
    }
}

// Remove JPA and other microservice-specific deps inherited from root
configurations {
    all {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-data-jpa")
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-amqp")
        exclude(group = "org.springdoc", module = "springdoc-openapi-starter-webflux-ui")
        exclude(group = "org.mapstruct", module = "mapstruct")
        exclude(group = "org.mapstruct", module = "mapstruct-processor")
        exclude(group = "org.projectlombok", module = "lombok-mapstruct-binding")
        exclude(group = "org.postgresql", module = "postgresql")
        exclude(group = "com.h2database", module = "h2")
    }
}

dependencies {
    // Spring Cloud Gateway (reactive, built on WebFlux + Netty)
    implementation("org.springframework.cloud:spring-cloud-starter-gateway")

    // Circuit Breaker (Resilience4j reactive)
    implementation("org.springframework.cloud:spring-cloud-starter-circuitbreaker-reactor-resilience4j")

    // Reactive Security (WebFlux-aware)
    implementation("org.springframework.boot:spring-boot-starter-security")

    // Actuator for health checks
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    // Caffeine cache for in-memory rate limiting
    implementation("com.github.ben-manes.caffeine:caffeine:3.1.8")

    // JWT validation
    implementation("io.jsonwebtoken:jjwt-api:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")

    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("io.projectreactor:reactor-test")
}
