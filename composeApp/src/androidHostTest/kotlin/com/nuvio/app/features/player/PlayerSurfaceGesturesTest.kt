package com.nuvio.app.features.player

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], manifest = "mew/AndroidManifest.xml", packageName = "com.mew.test")
class PlayerSurfaceGesturesTest {
    @get:Rule
    val compose = createComposeRule()

    private var taps = 0
    private var unlocks = 0

    private fun setContent(showUnlockTarget: Boolean = false) {
        compose.setContent {
            val controlsLocked = rememberUpdatedState(true)
            val noop = rememberUpdatedState<() -> Unit>({})
            Box(
                Modifier.size(320.dp, 180.dp)
                    .testTag("surface")
                    .playerSurfaceTapGestures(
                        layoutSize = IntSize(320, 180),
                        playerControlsLockedState = controlsLocked,
                        onSurfaceTap = rememberUpdatedState { _: Offset -> taps++ },
                        onSurfaceDoubleTap = rememberUpdatedState { _: Offset -> },
                        activateHoldToSpeedState = noop,
                        deactivateHoldToSpeedState = noop,
                        revealLockedOverlayState = noop,
                    )
                    .playerSurfaceDragGestures(
                        gestureController = null,
                        layoutSize = IntSize(320, 180),
                        sideGestureSystemEdgeExclusionPx = 0f,
                        playerControlsLockedState = controlsLocked,
                        touchGesturesEnabledState = rememberUpdatedState(true),
                        isHoldToSpeedGestureActiveState = rememberUpdatedState(false),
                        currentPositionMsState = rememberUpdatedState(30_000L),
                        currentDurationMsState = rememberUpdatedState(120_000L),
                        deactivateHoldToSpeedState = noop,
                        showHorizontalSeekPreviewState = rememberUpdatedState { _: Long, _: Long -> },
                        showBrightnessFeedbackState = rememberUpdatedState { _: Float -> },
                        showVolumeFeedbackState = rememberUpdatedState { _: PlayerAudioLevel -> },
                        currentVolumeBoostPercentState = rememberUpdatedState(100),
                        applyVolumeBoostPercentState = rememberUpdatedState { _: Int -> },
                        clearLiveGestureFeedbackState = noop,
                        revealLockedOverlayState = noop,
                        commitHorizontalSeekState = rememberUpdatedState { _: Long -> },
                    ),
            ) {
                if (showUnlockTarget) {
                    Box(
                        Modifier.align(Alignment.Center)
                            .size(72.dp)
                            .clickable { unlocks++ }
                            .testTag("unlock"),
                    )
                }
            }
        }
    }

    @Test
    fun lockedSurfaceTapSurvivesSmallFingerDrift() {
        setContent()
        compose.onNodeWithTag("surface").performTouchInput {
            down(center)
            moveBy(Offset(1f, 0f))
            up()
        }
        compose.runOnIdle { assertEquals(1, taps) }
    }

    @Test
    fun lockedStateDoesNotCancelTheUnlockButtonTap() {
        setContent(showUnlockTarget = true)
        compose.onNodeWithTag("unlock").performTouchInput {
            down(center)
            moveBy(Offset(1f, 0f))
            up()
        }
        compose.runOnIdle { assertEquals(1, unlocks) }
    }
}
