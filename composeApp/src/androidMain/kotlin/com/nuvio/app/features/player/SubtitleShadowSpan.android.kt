@file:OptIn(androidx.media3.common.util.UnstableApi::class)

package com.nuvio.app.features.player

import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.style.CharacterStyle
import android.text.style.UpdateAppearance
import androidx.media3.common.text.Cue
import androidx.media3.common.text.LanguageFeatureSpan

internal object SubtitleShadowCue {
    fun apply(cue: Cue, style: SubtitleStyleState): Cue {
        val strength = if (style.shadowEnabled) {
            style.shadowStrength.coerceIn(SUBTITLE_SHADOW_STRENGTH_MIN, SUBTITLE_SHADOW_STRENGTH_MAX)
        } else {
            0
        }
        val text = cue.text
        if (strength <= 0 || text.isNullOrEmpty() || cue.bitmap != null) return cue

        val spanned = SpannableString(text)
        spanned.getSpans(0, spanned.length, SubtitleShadowSpan::class.java).forEach(spanned::removeSpan)
        spanned.setSpan(SubtitleShadowSpan(strength), 0, spanned.length, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
        return cue.buildUpon().setText(spanned).build()
    }
}

private class SubtitleShadowSpan(private val strength: Int) :
    CharacterStyle(),
    UpdateAppearance,
    LanguageFeatureSpan {
    override fun updateDrawState(paint: TextPaint) {
        val unit = paint.textSize * SHADOW_UNIT_OF_TEXT_SIZE * strength
        paint.setShadowLayer(unit * 0.8f, unit, unit, SHADOW_COLOR)
    }

    private companion object {
        const val SHADOW_UNIT_OF_TEXT_SIZE = 0.025f
        const val SHADOW_COLOR = 0xD9000000.toInt()
    }
}
