plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
}
group = "net.matsudamper"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.2")
}
