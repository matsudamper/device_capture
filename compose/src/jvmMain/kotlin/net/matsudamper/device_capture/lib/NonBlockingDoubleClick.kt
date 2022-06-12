package net.matsudamper.device_capture.lib

import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.forEachGesture
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventTimeoutCancellationException
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput

fun Modifier.nonBlockingDoubleClick(onDoubleClick: () -> Unit): Modifier {
    return pointerInput(Unit) {
        forEachGesture {
            awaitPointerEventScope {
                awaitFirstDown()

                try {
                    withTimeout(500) {
                        awaitFirstDown()

                        while (true) {
                            val event = awaitPointerEvent()
                            val isUp = event.changes.all { it.changedToUp() }
                            if (isUp) {
                                onDoubleClick()
                            }
                        }
                    }
                } catch (_: PointerEventTimeoutCancellationException) {

                }
            }
        }
    }
}