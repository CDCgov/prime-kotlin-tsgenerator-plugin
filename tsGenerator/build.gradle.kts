/*
 * Copyright 2017 Alicia Boya García
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

group = "me.ntrrgc"
version = "1.1.3"

plugins {
    kotlin("jvm")
    `java-library`
    `maven-publish`
    id("org.jmailen.kotlinter") version "3.13.0"
}

val kotlinVersion = "1.8.0"
val spekVersion = "2.0.19"

repositories {
    maven("https://dl.bintray.com/jetbrains/spek")
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")

    testImplementation("com.winterbe:expekt:0.5.0")
    testImplementation("org.spekframework.spek2:spek-dsl-jvm:$spekVersion")
    testRuntimeOnly("org.spekframework.spek2:spek-runner-junit5:$spekVersion")
    testImplementation("com.google.code.findbugs:jsr305:3.0.1")
}

tasks.test {
    useJUnitPlatform {
        includeEngines("spek2")
    }
}

java {
    withSourcesJar()
}

kotlin {
    jvmToolchain(11)
}

publishing {
    publications {
        create<MavenPublication>("tsGenerator") {
            from(components["java"])
        }
    }

    repositories {
        maven {
            url = uri("./build/publications/maven-repo")
        }
    }
}