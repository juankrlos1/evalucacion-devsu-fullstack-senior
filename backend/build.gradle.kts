plugins {
    java
    id("org.springframework.boot") version "3.3.5" apply false
    id("io.spring.dependency-management") version "1.1.6" apply false
}

allprojects {
    group = "com.banking"
    version = "1.0.0"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "org.springframework.boot")
    apply(plugin = "io.spring.dependency-management")

    // Redirect build output outside OneDrive to avoid file locking issues
    layout.buildDirectory = file("${System.getProperty("java.io.tmpdir")}/gradle-build/${project.name}")

    java {
        toolchain {
            languageVersion = JavaLanguageVersion.of(17)
        }
    }

    configurations {
        compileOnly {
            extendsFrom(configurations.annotationProcessor.get())
        }
    }

    // Common dependencies for ALL subprojects
    dependencies {
        implementation("org.springframework.boot:spring-boot-starter-actuator")
        compileOnly("org.projectlombok:lombok")
        annotationProcessor("org.projectlombok:lombok")
        testImplementation("org.springframework.boot:spring-boot-starter-test")
        testImplementation("io.projectreactor:reactor-test")
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    tasks.withType<Copy> {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }
}

// ===== Microservice-specific config (excludes gateway) =====
configure(subprojects.filter { it.name != "bp-ms-gateway" }) {
    dependencies {
        implementation("org.springframework.boot:spring-boot-starter-webflux")
        implementation("org.springframework.boot:spring-boot-starter-data-jpa")
        implementation("org.springframework.boot:spring-boot-starter-validation")
        implementation("org.springframework.boot:spring-boot-starter-amqp")
        implementation("org.springdoc:springdoc-openapi-starter-webflux-ui:2.3.0")

        implementation("org.mapstruct:mapstruct:1.6.3")
        annotationProcessor("org.mapstruct:mapstruct-processor:1.6.3")
        annotationProcessor("org.projectlombok:lombok-mapstruct-binding:0.2.0")

        runtimeOnly("org.postgresql:postgresql")

        testRuntimeOnly("com.h2database:h2")
    }

    // Separate integration tests source set
    sourceSets {
        create("integrationTest") {
            java.srcDir("src/integrationTest/java")
            resources.srcDir("src/integrationTest/resources")
            compileClasspath += sourceSets["main"].output + configurations["testRuntimeClasspath"]
            runtimeClasspath += output + compileClasspath
        }
    }

    val integrationTestImplementation by configurations.getting {
        extendsFrom(configurations["testImplementation"])
    }
    val integrationTestRuntimeOnly by configurations.getting {
        extendsFrom(configurations["testRuntimeOnly"])
    }

    dependencies {
        integrationTestImplementation("com.intuit.karate:karate-junit5:1.4.1")
    }

    tasks.register<Test>("integrationTest") {
        description = "Runs Karate integration tests against running Docker services."
        group = "verification"
        testClassesDirs = sourceSets["integrationTest"].output.classesDirs
        classpath = sourceSets["integrationTest"].runtimeClasspath
        useJUnitPlatform()
    }

    tasks.withType<JavaCompile> {
        options.compilerArgs.addAll(listOf(
            "-Amapstruct.defaultComponentModel=spring",
            "-Amapstruct.unmappedTargetPolicy=IGNORE"
        ))
    }
}
