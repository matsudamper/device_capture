package net.matsudamper.device_capture.screen.main

import androidx.compose.animation.core.withInfiniteAnimationFrameMillis
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@Composable
fun MainScreen(
    modifier: Modifier,
    uiState: MainScreenUiState,
) {
    MaterialTheme {
        val coroutineScope = rememberCoroutineScope()
        val scaffoldState = rememberScaffoldState()

        LaunchedEffect(uiState.event) {
            coroutineScope.launch event@{
                val snackbarHostState = scaffoldState.snackbarHostState
                if (snackbarHostState.currentSnackbarData != null) return@event

                val event = uiState.event.filterIsInstance<MainScreenUiState.Event.Notification>()
                    .firstOrNull() ?: return@event

                uiState.consumeEvent(event)

                val result = snackbarHostState.showSnackbar(
                    message = event.text,
                )
            }
        }
        Scaffold(
            modifier = modifier,
            scaffoldState = scaffoldState,
        ) {
            Box(Modifier.fillMaxSize()) {
                var bitmapState: Painter? by remember {
                    mutableStateOf(null)
                }
                LaunchedEffect(uiState.image) {
                    while (isActive) {
                        withInfiniteAnimationFrameMillis {
                            bitmapState = uiState.image()
                        }
                    }
                }
                val defaultPainter = remember {
                    ColorPainter(Color.Green)
                }
                Image(
                    modifier = Modifier.fillMaxSize(),
                    painter = bitmapState ?: defaultPainter,
                    contentDescription = null
                )
            }
        }
    }
}


@Composable
@Preview
private fun Preview() {
    MainScreen(
        modifier = Modifier.fillMaxSize(),
        uiState = MainScreenUiState(
            name = "",
            event = listOf(),
            consumeEvent = {},
            onClickCameraChange = {},
            image = { null },
        ),
    )
}
