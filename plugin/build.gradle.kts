import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-gradle-plugin`
    kotlin("jvm") version "1.7.21"
    id("org.jlleitschuh.gradle.ktlint") version "11.0.0"
    id("maven-publish")
}

repositories {
    mavenCentral()
    gradlePluginPortal()
    maven {
        url = uri("https://jitpack.io")
    }
}

group = "gov.cdc.prime"
version = "0.1-SNAPSHOT"

dependencies {
    implementation("com.github.ntrrgc:ts-generator:1.1.2")
    implementation("io.github.classgraph:classgraph:4.8.153")
    testImplementation("com.fasterxml.jackson.core:jackson-databind:2.14.1")
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.1")
    api(project(":library"))
}

tasks.test {
    useJUnitPlatform()
}

// Fix gradle reflection warnings
tasks.withType<Test>().configureEach {
    jvmArgs(
        "--add-opens=java.base/java.lang=ALL-UNNAMED",
        "--add-opens=java.base/java.util=ALL-UNNAMED"
    )
}

ktlint {
    // See ktlint versions at https://github.com/pinterest/ktlint/releases
    version.set("0.43.2")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

gradlePlugin {
    plugins {
        create("tsGeneratorPlugin") {
            id = "gov.cdc.prime.tsGenerator.plugin"
            implementationClass = "gov.cdc.prime.tsGenerator.plugin.TypescriptGeneratorPlugin"
        }
    }
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/CDCgov/prime-kotlin-tsgenerator-plugin")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
