plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization") version "1.6.10"
}

group = "net.matsudamper"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
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
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.2")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.3")
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
                runtimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
            }
        }
    }
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}