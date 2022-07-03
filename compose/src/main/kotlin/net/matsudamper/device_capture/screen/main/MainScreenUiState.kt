package net.matsudamper.device_capture.screen.main

import androidx.compose.ui.graphics.painter.Painter

data class MainScreenUiState(
    val name: String,
    val onClickCameraChange: () -> Unit,
    val event: List<Event>,
    val image: () -> Painter?,
    val consumeEvent: (Event) -> Unit,
) {
    sealed interface Event {
        data class Notification(
            val text: String
        ) : Event
    }
}