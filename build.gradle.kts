import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm") version "1.6.10"
    id("org.jetbrains.compose") version "1.1.1"
}

group = "net.matsudamper"
version = rootProject.properties["net.matsudamper.device_capture.version"]
    .let { it as? String }
    .takeUnless { it.isNullOrBlank() } ?: "local"

val mainClassName = "net.matsudamper.device_capture.MainKt"

repositories {
    mavenLocal()
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    maven("https://maven.icm.edu.pl/artifactory/repo/")
}

dependencies {
    implementation(project("compose"))
    implementation(project("gstreamer"))
    implementation(project("data:local_json"))
    implementation(compose.desktop.currentOs)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.3.3")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.3")

    implementation("org.freedesktop.gstreamer:gst1-java-core:1.4.0")

    implementation("org.slf4j:slf4j-simple:1.7.36")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}

tasks.withType(Jar::class.java) {
    manifest {
        attributes["Main-Class"] = mainClassName
    }
    from(
        configurations.runtimeClasspath.map { config ->
            config.toList().orEmpty().map {
                if (it.isDirectory) it else zipTree(it)
            }
        }
    )
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

compose.desktop {
    application {
        mainClass = mainClassName
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            vendor = "matsudamper"
            packageName = "Device Capture"
            packageVersion = "1.0.0"
        }
    }
}
