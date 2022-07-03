package net.matsudamper.device_capture.screen

import androidx.compose.runtime.Immutable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.matsudamper.device_capture.Store
import net.matsudamper.device_capture.gstreamer.GStreamerNativeLibrary
import net.matsudamper.device_capture.logic.SettingsRepository
import net.matsudamper.device_capture.screen.main_window.MainWindowUiState
import net.matsudamper.device_capture.screen.select_device.SelectDeviceScreenViewModel
import kotlin.io.path.Path

class MainWindowViewModel(
    private val coroutineScope: CoroutineScope
) {
    private val uiMutableState = MutableStateFlow(
        MainWindowUiState(
            title = "Capture",
            onClickMainScreen = {
                overlayScreen.update {
                    getSelectedScreenUiState()
                }
            }
        )
    )
    val uiState = uiMutableState.asStateFlow()
    val overlayScreen: MutableStateFlow<OverlayScreens?> = MutableStateFlow(null)

    private val selectedWebCamFlow: MutableStateFlow<Unit?> = MutableStateFlow(null)

    private val settingsRepository = SettingsRepository(Path("./settings.json"))

    init {
        coroutineScope.launch {
            withContext(Dispatchers.IO) {
                GStreamerNativeLibrary.init()
            }

            withContext(Dispatchers.IO) {
                val settings = settingsRepository.get()
                Store.selectedVideoDevicePath.value = settings.videoDevicePath
                Store.selectedAudioInputDevicePath.value = settings.audioInputDevicePath
                Store.selectedAudioOutputDevicePath.value = settings.audioOutputDevicePath
            }

            val selectedWebCam = selectedWebCamFlow.value
            if (selectedWebCam == null) {
                overlayScreen.update {
                    getSelectedScreenUiState()
                }
            }
        }
    }

    private fun getSelectedScreenUiState(): OverlayScreens.SelectScreen {
        return OverlayScreens.SelectScreen(
            onDismissRequest = {
                overlayScreen.value = null
            },
            callback = object : SelectDeviceScreenViewModel.Argument {
                override val initialSelectedVideoPath: String? =
                    Store.selectedVideoDevicePath.value
                override val initialSelectedAudioInputPath: String? =
                    Store.selectedAudioInputDevicePath.value
                override val initialSelectedAudioOutputPath: String? =
                    Store.selectedAudioOutputDevicePath.value

                override fun selectedVideoDevicePath(string: String) {
                    Store.selectedVideoDevicePath.value = string
                    settingsRepository.update {
                        it.copy(videoDevicePath = string)
                    }
                }

                override fun selectedAudioInputDevice(path: String) {
                    Store.selectedAudioInputDevicePath.value = path
                    settingsRepository.update {
                        it.copy(audioInputDevicePath = path)
                    }
                }

                override fun selectedAudioOutputDevice(path: String) {
                    Store.selectedAudioOutputDevicePath.value = path
                    settingsRepository.update {
                        it.copy(audioOutputDevicePath = path)
                    }
                }
            }
        )
    }

    @Immutable
    sealed interface OverlayScreens {
        val onDismissRequest: () -> Unit

        @Immutable
        data class SelectScreen(
            override val onDismissRequest: () -> Unit,
            val callback: SelectDeviceScreenViewModel.Argument
        ) : OverlayScreens
    }
}
