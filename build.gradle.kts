/*
 * For more details on building Java & JVM projects, please refer to
 * https://docs.gradle.org/8.13/userguide/building_java_projects.html in the Gradle documentation.
 */

plugins {
    val kotlinVersion = "2.1.20"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion

    id("org.jetbrains.dokka") version "1.9.20"

    `java-library`
    // application // Java CLI application
}

group = "org.example"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

    compileOnly("org.jetbrains:annotations:26.0.2") // IDE support annotations

    testImplementation("org.junit.jupiter:junit-jupiter:5.12.1")
    testImplementation("org.assertj:assertj-core:3.27.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    jvmToolchain(17)
}

// application {
//     mainClass.set("org.example.MainKt")
// }

tasks.named<Test>("test") {
    useJUnitPlatform()
}

tasks.dokkaHtml {
    outputDirectory.set(file("$rootDir/docs"))

    dokkaSourceSets {
        named("main") {
            // Use DOC.md in root for package documentation.
            includes.from("DOC.md")
        }
    }
}

