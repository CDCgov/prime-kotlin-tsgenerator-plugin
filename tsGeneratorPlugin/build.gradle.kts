plugins {
    kotlin("jvm")
    id("org.jmailen.kotlinter") version "3.13.0"
    id("com.gradle.plugin-publish") version "1.0.0"
}

repositories {
    mavenCentral()
    gradlePluginPortal()
    maven {
        url = uri("https://jitpack.io")
    }
}

group = "com.github.CDCgov"
version = "0.1-SNAPSHOT"

dependencies {
    implementation(project(":tsGenerator"))
    implementation("io.github.classgraph:classgraph:4.8.154")
    testImplementation("com.fasterxml.jackson.core:jackson-databind:2.14.2")
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
}

tasks.test {
    useJUnitPlatform()
}

// Fix gradle reflection warnings
tasks.withType<Test>().configureEach {
    jvmArgs(
        "--add-opens=java.base/java.lang=ALL-UNNAMED",
        "--add-opens=java.base/java.util=ALL-UNNAMED",
    )
}

kotlin {
    jvmToolchain(11)
}

gradlePlugin {
    plugins {
        create("ts-generatorPlugin") {
            id = "com.github.CDCgov.prime-kotlin-tsgenerator-plugin"
            implementationClass = "gov.cdc.prime.tsGenerator.TypescriptGeneratorPlugin"
        }
    }
}

publishing {
    repositories {
        maven {
            url = uri("./build/publications/maven-repo")
        }
    }
}