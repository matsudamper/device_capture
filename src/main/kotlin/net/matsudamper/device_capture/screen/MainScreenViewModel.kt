package net.matsudamper.device_capture.screen

import androidx.compose.ui.graphics.toPainter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import net.matsudamper.device_capture.Store
import net.matsudamper.device_capture.gstreamer.audio.GstAudioIOManager
import net.matsudamper.device_capture.gstreamer.video.GstVideoDeviceStreamingManager
import net.matsudamper.device_capture.screen.main.MainScreenUiState

class MainScreenViewModel(
    private val coroutineScope: CoroutineScope
) {
    val mainScreenUiState: MutableStateFlow<MainScreenUiState> by lazy {
        MutableStateFlow(
            MainScreenUiState(
                name = "",
                event = listOf(),
                consumeEvent = { consumeEvent ->
                    mainScreenUiState.update { mainScreenUiState ->
                        mainScreenUiState.copy(
                            event = mainScreenUiState.event
                                .filterNot { it == consumeEvent }
                        )
                    }
                },
                onClickCameraChange = { },
                image = { null },
            )
        )
    }

    private var videoStreamingManagerFlow: MutableStateFlow<Pair<String, GstVideoDeviceStreamingManager>?> =
        MutableStateFlow(null)

    private var audioIOManagerFlow: MutableStateFlow<GstAudioIOManager?> =
        MutableStateFlow(null)

    init {

        coroutineScope.launch {
            launch {
                Store.selectedVideoDevicePath.collect { selectedPath ->
                    val streaming = videoStreamingManagerFlow.value
                    if (streaming != null) {
                        val (key, value) = streaming
                        if (selectedPath == key) return@collect
                        value.close()
                    }

                    if (selectedPath == null) return@collect

                    mainScreenUiState.update {
                        it.copy(
                            event = it.event.plus(
                                MainScreenUiState.Event.Notification(
                                    text = "Open Camera"
                                )
                            )
                        )
                    }

                    this@MainScreenViewModel.videoStreamingManagerFlow.value =
                        selectedPath to GstVideoDeviceStreamingManager(selectedPath)
                }
            }
            launch {
                combine(
                    Store.selectedAudioInputDevicePath,
                    Store.selectedAudioOutputDevicePath
                ) { input, output ->
                    audioIOManagerFlow.value?.close()

                    if (input == null) {
                        return@combine
                    }

                    audioIOManagerFlow.value = GstAudioIOManager(
                        wasapi2InputId = input,
                        wasapi2OutputId = output,
                    )
                }.collect()
            }
            launch {
                videoStreamingManagerFlow.filterNotNull().collect { (_, value) ->
                    val flow = value.getImageFlow()

                    mainScreenUiState.update {
                        it.copy(image = {
                            flow.value.toPainter()
                        })
                    }
                }
            }
        }
    }
}