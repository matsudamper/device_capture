package net.matsudamper.device_capture.lib

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*


fun <T, R> MutableStateFlow<T>.mapWithState(
    coroutineScope: CoroutineScope,
    transformer: (T) -> R
) : StateFlow<R> {
    return map { transformer(it) }
        .stateIn(
            scope = coroutineScope,
            started = SharingStarted.Lazily,
            initialValue = transformer(value)
        )
}