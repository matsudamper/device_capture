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

    implementation("org.freedesktop.gstreamer:gst1-java-core:1.4.0")
    implementation("org.freedesktop.gstreamer:gst1-java-fx:0.9.0")
    implementation("org.freedesktop.gstreamer:gst1-java-swing:0.9.0")
}
