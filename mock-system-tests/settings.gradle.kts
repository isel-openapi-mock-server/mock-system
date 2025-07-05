plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
rootProject.name = "mock-system-tests"


include(":admin-jvm", ":mock-jvm", ":mock-system-tests")

project(":admin-jvm").projectDir = file("admin/jvm")
project(":mock-jvm").projectDir = file("mock/jvm")
project(":mock-system-tests").projectDir = file("mock-system-tests")

