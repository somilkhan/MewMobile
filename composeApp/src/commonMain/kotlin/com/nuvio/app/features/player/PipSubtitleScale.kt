package com.nuvio.app.features.player

/**
 * Computes the displayed height of a fitted video within the subtitle view bounds.
 * Returns null until both view and video geometry are valid.
 */
internal fun calculateFittedVideoHeightPx(
    viewWidthPx: Float,
    viewHeightPx: Float,
    videoWidthPx: Int,
    videoHeightPx: Int,
    pixelWidthHeightRatio: Float,
): Float? {
    if (
        !viewWidthPx.isFinite() || !viewHeightPx.isFinite() ||
        viewWidthPx <= 0f || viewHeightPx <= 0f ||
        videoWidthPx <= 0 || videoHeightPx <= 0 ||
        !pixelWidthHeightRatio.isFinite() || pixelWidthHeightRatio <= 0f
    ) {
        return null
    }
    val videoAspect = videoWidthPx.toFloat() * pixelWidthHeightRatio / videoHeightPx
    if (!videoAspect.isFinite() || videoAspect <= 0f) return null
    val displayedHeight = minOf(viewHeightPx, viewWidthPx / videoAspect)
    return displayedHeight.takeIf { it.isFinite() && it > 0f }
}
