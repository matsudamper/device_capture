package net.matsudamper.device_capture.screen.select_device

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.matsudamper.device_capture.gstreamer.audio.GetGstAudioInputUseCase
import net.matsudamper.device_capture.gstreamer.audio.GetGstAudioOutputUseCase
import net.matsudamper.device_capture.gstreamer.video.GetGstVideoSourceListUseCase
import net.matsudamper.device_capture.lib.mapWithState
import net.matsudamper.device_capture.screen.settings.SettingsUiState

class SelectDeviceScreenViewModel(
    private val coroutineScope: CoroutineScope,
    private val argument: Argument,
) {
    val event = Channel<Event>(Channel.UNLIMITED)

    private val uiMutableStateFlow: MutableStateFlow<SettingsUiState> = MutableStateFlow(
        SettingsUiState(
            currentTabType = SettingsUiState.SettingsScreenTabType.Display,
            currentPage = SettingsUiState.SettingTab.Display(
                itemsUiState = listOf(),
                isLoading = true,
            ),
            onDismissRequest = {
                event.trySend(Event.DismissRequest)
            },
            onClickTab = { tabType ->
                viewModelStateFlow.update {
                    it.copy(
                        currentTabType = tabType
                    )
                }
            }
        )
    )
    val uiStateFlow = uiMutableStateFlow.asStateFlow()

    private val viewModelStateFlow = MutableStateFlow(ViewModelState())

    init {
        coroutineScope.launch {
            launch { fetchDataWhenCurrentTabChange() }

            launch {
                viewModelStateFlow.map { it.currentTabType }
                    .stateIn(this)
                    .collect { tabType ->
                        uiMutableStateFlow.update { uiState ->
                            uiState.copy(
                                currentTabType = tabType
                            )
                        }
                    }
            }

            launch { updateDisplayTabContentByTabChange() }
            launch { updateAudioInputTabContentByTabChange() }
            launch { updateAudioOutputTabContentByTabChange() }
        }
    }

    private suspend fun updateAudioOutputTabContentByTabChange() {
        combine(
            flows = arrayOf(
                viewModelStateFlow.map { it.currentTabType },
                viewModelStateFlow.map { it.audioOutputData },
            )
        ) {
            val viewModelState = viewModelStateFlow.value
            if (viewModelState.currentTabType != SettingsUiState.SettingsScreenTabType.AudioOutput) return@combine

            val audioOutputData = viewModelState.audioOutputData
            println(audioOutputData)
            if (audioOutputData.isInitialized.not()) {
                uiMutableStateFlow.update { uiState ->
                    uiState.copy(
                        currentPage = SettingsUiState.SettingTab.AudioInput(
                            itemsUiState = listOf(),
                            isLoading = true,
                        ),
                    )
                }
            } else {
                uiMutableStateFlow.update { uiState ->
                    uiState.copy(
                        currentPage = SettingsUiState.SettingTab.AudioOutput(
                            itemsUiState = createItemUiStateByAudioOutput(
                                audioOutputItems = audioOutputData.sourceList,
                                selected = audioOutputData.selectedAudioOutputDevicePath,
                                onSelected = { selectedPath ->
                                    argument.selectedAudioOutputDevice(selectedPath)
                                    viewModelStateFlow.update {
                                        it.copy(
                                            audioOutputData = it.audioOutputData.copy(
                                                selectedAudioOutputDevicePath = selectedPath
                                            )
                                        )
                                    }
                                }
                            ),
                            isLoading = audioOutputData.isInitialized.not()
                        ),
                    )
                }
            }
        }.collect()
    }

    private suspend fun updateAudioInputTabContentByTabChange() {
        combine(
            flows = arrayOf(
                viewModelStateFlow.map { it.currentTabType },
                viewModelStateFlow.map { it.audioInputData },
            )
        ) {
            val viewModelState = viewModelStateFlow.value
            if (viewModelState.currentTabType != SettingsUiState.SettingsScreenTabType.AudioInput) return@combine

            val audioInputData = viewModelState.audioInputData
            println(audioInputData)
            if (audioInputData.isInitialized.not()) {
                uiMutableStateFlow.update { uiState ->
                    uiState.copy(
                        currentPage = SettingsUiState.SettingTab.AudioInput(
                            itemsUiState = listOf(),
                            isLoading = true,
                        ),
                    )
                }
            } else {
                uiMutableStateFlow.update { uiState ->
                    uiState.copy(
                        currentPage = SettingsUiState.SettingTab.AudioInput(
                            itemsUiState = createItemUiStateByAudioInput(
                                audioInputItems = audioInputData.sourceList,
                                selected = audioInputData.selectedAudioInputDevicePath,
                                onSelected = { selectedPath ->
                                    argument.selectedAudioInputDevice(selectedPath)
                                    viewModelStateFlow.update {
                                        it.copy(
                                            audioInputData = it.audioInputData.copy(
                                                selectedAudioInputDevicePath = selectedPath
                                            )
                                        )
                                    }
                                }
                            ),
                            isLoading = audioInputData.isInitialized.not()
                        ),
                    )
                }
            }
        }.collect()
    }

    private suspend fun updateDisplayTabContentByTabChange() {
        combine(
            flows = arrayOf(
                viewModelStateFlow.map { it.currentTabType },
                viewModelStateFlow.map { it.displayData },
            )
        ) {
            val viewModelState = viewModelStateFlow.value
            if (viewModelState.currentTabType != SettingsUiState.SettingsScreenTabType.Display) return@combine

            val displayData = viewModelState.displayData
            if (displayData.isInitialized.not()) {
                uiMutableStateFlow.update { uiState ->
                    uiState.copy(
                        currentPage = SettingsUiState.SettingTab.Display(
                            itemsUiState = listOf(),
                            isLoading = true,
                        ),
                    )
                }
            } else {
                uiMutableStateFlow.update { uiState ->
                    uiState.copy(
                        currentPage = SettingsUiState.SettingTab.Display(
                            itemsUiState = createItemUiStateByCamera(
                                cameraList = displayData.sourceList,
                                selectedVideoDevicePath = displayData.selectedVideoDevicePath,
                                onSelectedVideoDevicePath = { selectedPath ->
                                    argument.selectedVideoDevicePath(selectedPath)
                                    viewModelStateFlow.update {
                                        it.copy(
                                            displayData = it.displayData.copy(
                                                selectedVideoDevicePath = selectedPath
                                            )
                                        )
                                    }
                                }
                            ),
                            isLoading = false
                        ),
                    )
                }
            }
        }.collect()
    }


    private suspend fun fetchDataWhenCurrentTabChange() {
        val getVideoSourceUseCase = GetGstVideoSourceListUseCase()
        val getAudioInputSourceUseCase = GetGstAudioInputUseCase()
        val getAudioOutputSourceUseCase = GetGstAudioOutputUseCase()

        viewModelStateFlow.mapWithState(coroutineScope) {
            it.currentTabType
        }.collect {
            val viewModelState = viewModelStateFlow.value

            when (viewModelState.currentTabType) {
                SettingsUiState.SettingsScreenTabType.Display -> {
                    if (viewModelState.displayData.isInitialized) return@collect
                    val videoSourceList = withContext(Dispatchers.IO) {
                        getVideoSourceUseCase.getMfVideoSource()
                    }

                    val includeInitialValue = videoSourceList
                        .any { it.devicePath == argument.initialSelectedVideoPath }

                    viewModelStateFlow.update {
                        it.copy(
                            displayData = it.displayData.copy(
                                isInitialized = true,
                                sourceList = videoSourceList,
                                selectedVideoDevicePath = argument.initialSelectedVideoPath
                                    .takeIf { includeInitialValue },
                            )
                        )
                    }

                }
                SettingsUiState.SettingsScreenTabType.AudioInput -> {
                    if (viewModelState.audioInputData.isInitialized) return@collect
                    val audioSourceList = withContext(Dispatchers.IO) {
                        getAudioInputSourceUseCase.getWasApi2SourceList()
                    }

                    val includeInitialValue = audioSourceList
                        .any { it.deviceId == argument.initialSelectedAudioInputPath }

                    viewModelStateFlow.update {
                        it.copy(
                            audioInputData = it.audioInputData.copy(
                                isInitialized = true,
                                sourceList = audioSourceList,
                                selectedAudioInputDevicePath = argument.initialSelectedAudioInputPath
                                    .takeIf { includeInitialValue },
                            )
                        )
                    }
                }
                SettingsUiState.SettingsScreenTabType.AudioOutput -> {
                    if (viewModelState.audioOutputData.isInitialized) return@collect
                    val audioSourceList = withContext(Dispatchers.IO) {
                        getAudioOutputSourceUseCase.getWasApi2SourceList()
                    }

                    val includeInitialValue = audioSourceList
                        .any { it.deviceId == argument.initialSelectedAudioOutputPath }

                    viewModelStateFlow.update {
                        it.copy(
                            audioOutputData = it.audioOutputData.copy(
                                isInitialized = true,
                                sourceList = audioSourceList,
                                selectedAudioOutputDevicePath = argument.initialSelectedAudioOutputPath
                                    .takeIf { includeInitialValue }
                            )
                        )
                    }
                }
            }
        }
    }

    private data class ViewModelState(
        val currentTabType: SettingsUiState.SettingsScreenTabType = SettingsUiState.SettingsScreenTabType.Display,
        val displayData: DisplayTabData = DisplayTabData(),
        val audioInputData: AudioInputTabData = AudioInputTabData(),
        val audioOutputData: AudioOutputTabData = AudioOutputTabData(),
    ) {
        data class DisplayTabData(
            val isInitialized: Boolean = false,
            val sourceList: List<GetGstVideoSourceListUseCase.VideoSource> = listOf(),
            val selectedVideoDevicePath: String? = null,
        )

        data class AudioInputTabData(
            val isInitialized: Boolean = false,
            val sourceList: List<GetGstAudioInputUseCase.AudioInputSource> = listOf(),
            val selectedAudioInputDevicePath: String? = null,
        )

        data class AudioOutputTabData(
            val isInitialized: Boolean = false,
            val sourceList: List<GetGstAudioOutputUseCase.AudioOutputSource> = listOf(),
            val selectedAudioOutputDevicePath: String? = null,
        )
    }

    sealed interface Event {
        object DismissRequest : Event
    }

    interface Argument {
        val initialSelectedVideoPath: String?
        val initialSelectedAudioInputPath: String?
        val initialSelectedAudioOutputPath: String?
        fun selectedVideoDevicePath(string: String)
        fun selectedAudioInputDevice(path: String)
        fun selectedAudioOutputDevice(path: String)
    }
}

private fun createItemUiStateByCamera(
    cameraList: List<GetGstVideoSourceListUseCase.VideoSource>,
    selectedVideoDevicePath: String?,
    onSelectedVideoDevicePath: (String) -> Unit,
): List<SettingsUiState.NameUiState> {
    return cameraList.map { camera ->
        SettingsUiState.NameUiState(
            name = camera.displayName,
            isSelected = selectedVideoDevicePath != null && camera.devicePath == selectedVideoDevicePath,
            onClick = {
                onSelectedVideoDevicePath(camera.devicePath)
            },
        )
    }
}

private fun createItemUiStateByAudioInput(
    audioInputItems: List<GetGstAudioInputUseCase.AudioInputSource>,
    selected: String?,
    onSelected: (String) -> Unit,
): List<SettingsUiState.NameUiState> {
    return audioInputItems.map { camera ->
        SettingsUiState.NameUiState(
            name = camera.displayName,
            isSelected = selected != null && camera.deviceId == selected,
            onClick = {
                onSelected(camera.deviceId)
            },
        )
    }
}

private fun createItemUiStateByAudioOutput(
    audioOutputItems: List<GetGstAudioOutputUseCase.AudioOutputSource>,
    selected: String?,
    onSelected: (String) -> Unit,
): List<SettingsUiState.NameUiState> {
    return audioOutputItems.map { camera ->
        SettingsUiState.NameUiState(
            name = camera.displayName,
            isSelected = selected != null && camera.deviceId == selected,
            onClick = {
                onSelected(camera.deviceId)
            },
        )
    }
}
