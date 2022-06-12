package net.matsudamper.device_capture.data.model.settings

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Settings(
    @SerialName("video_device_path") val videoDevicePath: String?,
    @SerialName("audio_input_device_path") val audioInputDevicePath: String?,
    @SerialName("audio_output_device_path") val audioOutputDevicePath: String?,
) : SettingsBaseInfo(version = "1")