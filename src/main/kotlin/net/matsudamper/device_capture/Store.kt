package net.matsudamper.device_capture

import kotlinx.coroutines.flow.MutableStateFlow

object Store {
    @Deprecated("")
    val selectedCameraName : MutableStateFlow<String?> = MutableStateFlow(null)

    val selectedVideoDevicePath : MutableStateFlow<String?> = MutableStateFlow(null)

    val selectedAudioInputDevicePath : MutableStateFlow<String?> = MutableStateFlow(null)
    val selectedAudioOutputDevicePath : MutableStateFlow<String?> = MutableStateFlow(null)
}