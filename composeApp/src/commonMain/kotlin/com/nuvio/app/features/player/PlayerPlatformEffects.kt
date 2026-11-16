package com.nuvio.app.features.player

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface PlayerGestureController {
    fun currentBrightness(): Float?
    fun setBrightness(level: Float): Float?
    fun currentVolume(): PlayerAudioLevel?
    fun setVolume(level: Float): PlayerAudioLevel?
}

data class PlayerAudioLevel(
    val fraction: Float,
    val isMuted: Boolean,
)

object PlayerOrientationPreference {
    private val _portrait = MutableStateFlow(false)
    private var originalRequestedOrientation: Int? = null
    val portrait: StateFlow<Boolean> = _portrait.asStateFlow()

    fun toggle() { _portrait.value = !_portrait.value }

    internal fun captureOriginalOrientation(requestedOrientation: Int): Int {
        if (originalRequestedOrientation == null) {
            originalRequestedOrientation = requestedOrientation
        }
        return originalRequestedOrientation ?: requestedOrientation
    }

    internal fun reset() {
        _portrait.value = false
        originalRequestedOrientation = null
    }
}

@Composable
expect fun LockPlayerToLandscape()

@Composable
expect fun rememberPlayerOrientationToggleSupported(): Boolean

@Composable
expect fun EnterImmersivePlayerMode(keepScreenAwake: Boolean)

@Composable
expect fun ManagePlayerPictureInPicture(
    isPlaying: Boolean,
    videoSize: IntSize,
)

@Composable
expect fun rememberIsInPictureInPicture(): Boolean

@Composable
expect fun rememberPlayerGestureController(): PlayerGestureController?
