plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    kotlin("plugin.serialization") version "1.9.25"
    id("org.springframework.boot") version "3.1.4"
    id("io.spring.dependency-management") version "1.1.7"
}


group = "diogo"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.slf4j:slf4j-simple:1.7.36") // Usado para os loggers.

    implementation("io.ktor:ktor-client-core:2.3.2")
    implementation("io.ktor:ktor-client-cio:2.3.2")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.2")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

    implementation("org.springframework.boot:spring-boot-starter") // starter do Spring Boot
    implementation("org.springframework.boot:spring-boot-starter-web") // spring mvc e tomcat para apps web
    implementation("org.springframework.data:spring-data-commons") // classes comuns do Spring Data

    implementation("org.reactivestreams:reactive-streams:1.0.4")
    implementation("io.projectreactor:reactor-core:3.6.5")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.8.1")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.9.23")

    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.4")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")


    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}