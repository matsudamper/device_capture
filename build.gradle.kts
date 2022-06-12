import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("multiplatform") version "1.6.10"
    id("org.jetbrains.compose") version "1.1.1"
}

group = "net.matsudamper"
version = "local"

repositories {
    mavenLocal()
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    maven("https://maven.icm.edu.pl/artifactory/repo/")
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "17"
        }
        withJava()
    }
    sourceSets {
        val jvmMain by getting {
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
        }
        val jvmTest by getting {

        }
    }
}

compose.desktop {
    application {
        mainClass = "net.matsudamper.device_capture.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "device_capture"
            packageVersion = "1.0.0"
        }
    }
}
