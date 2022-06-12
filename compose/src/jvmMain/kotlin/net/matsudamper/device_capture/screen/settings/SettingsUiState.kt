package net.matsudamper.device_capture.screen.settings

data class SettingsUiState(
    val currentTabType: SettingsScreenTabType,
    val currentPage: SettingTab,
    val onDismissRequest: () -> Unit,
    val onClickTab: (SettingsScreenTabType) -> Unit,
) {

    sealed interface SettingTab {
        val isLoading: Boolean
        data class Display(
            val itemsUiState: List<NameUiState>,
            override val isLoading: Boolean,
        ) : SettingTab

        data class AudioInput(
            val itemsUiState: List<NameUiState>,
            override val isLoading: Boolean,
        ) : SettingTab

        data class AudioOutput(
            val itemsUiState: List<NameUiState>,
            override val isLoading: Boolean,
        ) : SettingTab
    }

    data class NameUiState(
        val name: String,
        val isSelected: Boolean,
        val onClick: () -> Unit,
    )

    enum class SettingsScreenTabType(internal val title: String) {
        Display(title = "Display"),
        AudioInput(title = "Audio Input"),
        AudioOutput(title = "Audio Output")
    }
}