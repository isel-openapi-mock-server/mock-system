plugins {
	kotlin("jvm") version "1.9.25"
	kotlin("plugin.spring") version "1.9.25"
	id("org.springframework.boot") version "3.4.3"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "isel.openapi.mock"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("io.swagger.parser.v3:swagger-parser:2.1.16")
	implementation("commons-io:commons-io:2.15.0")
	implementation("com.google.guava:guava:32.1.2-jre")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("com.github.erosb:json-sKema:0.20.0")
	// https://mvnrepository.com/artifact/com.networknt/json-schema-validator
	implementation("com.networknt:json-schema-validator:1.5.7")
	implementation("com.google.code.gson:gson:2.13.0")
	implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.5.0")
	implementation("com.github.jknack:handlebars:4.3.1")
	implementation("org.apache.commons:commons-text:1.10.0")

	// for JDBI
	implementation("org.jdbi:jdbi3-core:3.37.1")
	implementation("org.jdbi:jdbi3-kotlin:3.37.1")
	implementation("org.jdbi:jdbi3-postgres:3.37.1")
	implementation("org.postgresql:postgresql:42.7.2")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	implementation("org.springframework.boot:spring-boot-starter-web")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webflux")
    testImplementation(kotlin("test"))
}

kotlin {
	jvmToolchain(21)
}

tasks.withType<Test> {
	useJUnitPlatform()
}

task<Exec>("dbTestsUp") {
    commandLine("docker", "compose", "up", "-d", "--build", "--force-recreate", "db-mock-tests", "mock-db")
}

task<Exec>("dbTestsWait") {
    commandLine("docker", "exec", "db-mock-tests", "/app/bin/wait-for-postgres.sh", "localhost")
	dependsOn("dbTestsUp")
}

task<Exec>("dbWait") {
	commandLine("docker", "exec", "mock-db", "/app/bin/wait-for-postgres.sh", "localhost")
	dependsOn("dbTestsUp")
}

task<Exec>("dbTestsDown") {
    commandLine("docker", "compose", "down")
}

tasks.named("check") {
    dependsOn("dbTestsWait")
	dependsOn("dbWait")
    finalizedBy("dbTestsDown")
}