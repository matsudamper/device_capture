package net.matsudamper.device_capture.screen.util

import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable

@Composable
@NonRestartableComposable
fun DarkMaterialTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colors = darkColors(),
        content = content,
    )
}
