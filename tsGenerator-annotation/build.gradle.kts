repositories {
    mavenCentral()
    gradlePluginPortal()
}

plugins {
    `java-library`
    kotlin("jvm") version "1.8.20"
    id("org.jlleitschuh.gradle.ktlint") version "11.1.0"
}

ktlint {
    // See ktlint versions at https://github.com/pinterest/ktlint/releases
    version.set("0.44.0")
}

kotlin {
    jvmToolchain(11)
}

group = "gov.cdc.prime"
version = "0.1-SNAPSHOT"
