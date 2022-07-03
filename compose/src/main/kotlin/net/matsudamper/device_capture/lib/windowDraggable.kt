package net.matsudamper.device_capture.screen.util

import androidx.compose.foundation.gestures.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.minus
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import kotlinx.coroutines.coroutineScope

@Composable
fun Modifier.windowDraggable(windowProvider: () -> WindowState): Modifier {
    val density = LocalDensity.current
    return pointerInput(density) {
        coroutineScope {
            forEachGesture {
                awaitPointerEventScope {
                    val firstDown = awaitFirstDown()
                    val window = windowProvider()
                    val firstPosition = firstDown.position
                    drag(
                        pointerId = firstDown.id,
                        onDrag = {
                            val diff: Offset = it.position - firstPosition

                            window.position = WindowPosition(
                                x = window.position.x + with(density) { diff.x.toDp() },
                                y = window.position.y + with(density) { diff.y.toDp() },
                            )
                        }
                    )
                }
            }
        }
    }
}
