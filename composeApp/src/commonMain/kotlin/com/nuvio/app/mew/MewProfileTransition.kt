package com.nuvio.app

// Mew glue for luq's profile-select transition (AppGate.kt / AppShellComponents.kt in
// 1.1.0-beta.3). luq keeps the tap origin inside AppGate's own state because his overlay lives
// there; Mew's launch overlay lives in MainAppContent, so the origin is handed across the gate
// through this small holder instead. Kept in its own file so luq merges of App.kt-equivalents
// don't collide with it.

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow

/** Window-space centre of the avatar the user just tapped, plus whether a pick happened at all. */
internal data class MewProfileTransitionStart(val origin: Offset?)

internal object MewProfileTransition {
    private var pending: MewProfileTransitionStart? = null

    /**
     * Window-space centre of the Compose profile tab avatar (bottom bar, floating bar, sidebar).
     * Android equivalent of luq's NativeTabBridge.profileTabIconFrame, which only Swift feeds.
     */
    val profileTabCenterInWindow = mutableStateOf<Offset?>(null)

    /** Called by the gate when a profile is picked on the "Who's watching" screen. */
    fun begin(origin: Offset?) {
        pending = MewProfileTransitionStart(origin)
    }

    /** Read once by the launch overlay; null means a cold start / auto-selected profile. */
    fun consume(): MewProfileTransitionStart? = pending.also { pending = null }
}

/** Publishes this element's centre as the profile tab target for the exit glide. */
internal fun Modifier.mewReportProfileTabCenter(): Modifier = onGloballyPositioned { coordinates ->
    if (coordinates.size.width == 0 || coordinates.size.height == 0) return@onGloballyPositioned
    MewProfileTransition.profileTabCenterInWindow.value = coordinates.positionInWindow() + Offset(
        coordinates.size.width / 2f,
        coordinates.size.height / 2f,
    )
}
