package net.matsudamper.device_capture.data.model.settings

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
abstract class SettingsBaseInfo(
    @SerialName("version") val version: String
)