package net.matsudamper.device_capture.logic

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.matsudamper.device_capture.data.model.settings.Settings
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.runCatching

class SettingsRepository(private val path: Path) {
    init {
        if (path.exists().not()) {
            path.writeText("")
        }
    }

    fun update(block: (Settings) -> Settings) {
        val newModel = block(deserialize(read()) ?: defaultValue)
        write(serialize(newModel))
    }

    fun get(): Settings = deserialize(read()) ?: defaultValue

    private fun read(): String {
        return path.readText()
    }

    private fun write(text: String) {
        path.writeText(text)
    }

    private fun serialize(value: Settings): String {
        return Json.encodeToString(value)
    }

    private fun deserialize(value: String): Settings? {
        return runCatching<Settings?> {
            Json.decodeFromString(value)
        }.getOrNull()
    }

    private val defaultValue = Settings(
        videoDevicePath = null,
        audioInputDevicePath = null,
        audioOutputDevicePath = null,
    )
}