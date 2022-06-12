package net.matsudamper.device_capture

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import kotlinx.coroutines.flow.*
import net.matsudamper.device_capture.data.model.settings.Settings
import net.matsudamper.device_capture.lib.nonBlockingDoubleClick
import net.matsudamper.device_capture.screen.MainScreenViewModel
import net.matsudamper.device_capture.screen.main.MainScreen
import net.matsudamper.device_capture.screen.MainWindowViewModel
import net.matsudamper.device_capture.screen.select_device.SelectDeviceScreenViewModel
import net.matsudamper.device_capture.screen.settings.SettingsScreen
import net.matsudamper.device_capture.screen.util.DarkMaterialTheme
import net.matsudamper.device_capture.screen.util.windowDraggable

fun main() = application {
    val rootCoroutineScope = rememberCoroutineScope()
    val mainWindowViewModel = remember { MainWindowViewModel(rootCoroutineScope) }
    val windowState = rememberWindowState()
    LaunchedEffect(Unit) {
        Settings(
            videoDevicePath = "1",
            audioInputDevicePath = "2",
            audioOutputDevicePath = "3",
        )
    }
    val windowUiState by mainWindowViewModel.uiState.collectAsState()
    Window(
        title = windowUiState.title,
        state = windowState,
        undecorated = true,
        onCloseRequest = {
            exitApplication()
        },
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
                .windowDraggable { windowState }
        ) {
            val mainScreenViewModel = remember {
                MainScreenViewModel(rootCoroutineScope)
            }
            MainScreen(
                modifier = Modifier.fillMaxSize()
                    .nonBlockingDoubleClick {
                        windowUiState.onClickMainScreen()
                    },
                uiState = mainScreenViewModel.mainScreenUiState.collectAsState().value,
            )

            val overlayScreen by mainWindowViewModel.overlayScreen.collectAsState()
            Overlay(
                overlayScreen = overlayScreen,
            )
        }
    }
}

@Composable
private fun Overlay(
    overlayScreen: MainWindowViewModel.OverlayScreens?,
) {
    if (overlayScreen != null) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Black.copy(alpha = 0.7f),
            contentColor = Color.White,
        ) {
            DarkMaterialTheme {
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    when (overlayScreen) {
                        is MainWindowViewModel.OverlayScreens.SelectScreen -> {
                            val coroutineScope = rememberCoroutineScope()
                            val selectedScreenViewModel = remember {
                                SelectDeviceScreenViewModel(
                                    coroutineScope = coroutineScope,
                                    argument = overlayScreen.callback
                                )
                            }
                            LaunchedEffect(selectedScreenViewModel) {
                                selectedScreenViewModel.event.receiveAsFlow().collect { event ->
                                    when (event) {
                                        SelectDeviceScreenViewModel.Event.DismissRequest -> {
                                            overlayScreen.onDismissRequest()
                                        }
                                    }
                                }
                            }

                            SettingsScreen(
                                modifier = Modifier.fillMaxSize(),
                                uiState = selectedScreenViewModel.uiStateFlow.collectAsState().value,
                            )
                        }
                    }.let { }
                }
            }
        }
    }
}
