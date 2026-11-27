package com.nuvio.app.core.ui

// Mew-only: hand-drawn sakura (cherry blossom) art for the Sumi theme.
// Everything is drawn procedurally so it stays crisp at every density and needs no assets:
//  - Modifier.sumiBlossomVine wraps a tapered ink branch with blossoms around one rounded end
//    of a button. It is a no-op for every other theme.
//  - drawSumiSakura / drawSumiBud are reused by the theme picker swatch and the
//    "Who's watching" backdrop so the three places share one drawing style.

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/** Colours for one blossom + branch rendering. */
internal data class SumiBlossomInk(
    val bark: Color,
    val barkHighlight: Color,
    val petalEdge: Color,
    val petalHeart: Color,
    val petalLine: Color,
    val eye: Color,
    val stamen: Color,
    val sepal: Color,
)

internal object SumiBlossomColors {
    /** Branch drawn on the light washi (primary) buttons: dark ink bark, white blossoms. */
    val OnWashi = SumiBlossomInk(
        bark = Color(0xFF2E2427),
        barkHighlight = Color(0xFF6E5B5F),
        petalEdge = Color(0xFFFFFBF8),
        petalHeart = Color(0xFFF1D3D6),
        petalLine = Color(0xFF8C7275),
        eye = Color(0xFFB0707C),
        stamen = Color(0xFF7D4752),
        sepal = Color(0xFF4A3436),
    )

    /** Branch drawn on the dark ink (secondary) buttons: weathered grey-brown bark. */
    val OnInk = SumiBlossomInk(
        bark = Color(0xFF6F5F61),
        barkHighlight = Color(0xFFA8979A),
        petalEdge = Color(0xFFFBF5F2),
        petalHeart = Color(0xFFE7C3C8),
        petalLine = Color(0x33000000),
        eye = Color(0xFFB0707C),
        stamen = Color(0xFF8A5560),
        sepal = Color(0xFF5A4245),
    )
}

/**
 * Wraps a sakura branch around one rounded end of a button when the Sumi theme is active.
 * Apply it before `clip` in the modifier chain so the branch can grow past the button edge.
 *
 * @param onAccent true for the light washi primary button, false for dark secondary buttons.
 * @param mirrored grow from the right end instead of the left (used for the second button of a pair).
 * @param cornerRadius button corner radius; null means a full pill (height / 2).
 */
@Composable
fun Modifier.sumiBlossomVine(
    onAccent: Boolean,
    mirrored: Boolean = false,
    cornerRadius: Dp? = null,
): Modifier {
    if (MaterialTheme.appTheme != AppTheme.SUMI) return this
    val ink = if (onAccent) SumiBlossomColors.OnWashi else SumiBlossomColors.OnInk
    return this.drawWithCache {
        val radiusPx = cornerRadius?.toPx()?.coerceAtMost(size.height / 2f) ?: (size.height / 2f)
        // Artwork is tuned for a 52dp tall button and scales with smaller or larger ones.
        val unit = size.height / (52f * density)
        onDrawWithContent {
            drawContent()
            if (size.width > 0f && size.height > 0f) {
                drawBlossomVine(size, radiusPx, unit, mirrored, ink)
            }
        }
    }
}

private fun DrawScope.drawBlossomVine(
    size: Size,
    radius: Float,
    unit: Float,
    mirrored: Boolean,
    ink: SumiBlossomInk,
) {
    val outset = 1.6f * unit * density
    val dp = unit * density
    val w = size.width
    val h = size.height
    // How far the branch runs along the top and bottom edges, kept clear of centred labels.
    val topReach = min(w * 0.34f, radius + 96f * dp)
    val bottomReach = min(w * 0.24f, radius + 64f * dp)

    val outline = Path().apply {
        moveTo(topReach, -outset)
        lineTo(radius, -outset)
        arcTo(Rect(-outset, -outset, 2f * radius + outset, 2f * radius + outset), 270f, -90f, false)
        lineTo(-outset, h - radius)
        arcTo(Rect(-outset, h - 2f * radius - outset, 2f * radius + outset, h + outset), 180f, -90f, false)
        lineTo(bottomReach, h + outset)
    }
    val measure = PathMeasure().apply { setPath(outline, false) }
    val length = measure.length
    if (length <= 0f) return

    fun mirror(o: Offset) = if (mirrored) Offset(w - o.x, o.y) else o
    fun outwardNormal(distance: Float): Offset {
        val t = measure.getTangent(distance)
        // Path runs counter-clockwise around the left end, so the outward side is (-t.y, t.x).
        return Offset(-t.y, t.x)
    }

    // Bark: thickest where it hugs the rounded end, tapering to both tips.
    // Short straight segments (about every 8dp) so the bark follows the rounded end while
    // the alternating jitter keeps it from looking like a perfect geometric outline.
    val nodeCount = (length / (8f * dp)).toInt().coerceIn(12, 40)
    val nodes = (0..nodeCount).map { i ->
        val f = i / nodeCount.toFloat()
        val d = f * length
        val n = outwardNormal(d)
        val jitter = (if (i % 2 == 0) 0.55f else -0.4f) * dp * sin(f * PI.toFloat() * 5.3f + 0.6f)
        measure.getPosition(d) + n * jitter
    }
    val maxWidth = 3.4f * dp
    for (i in 0 until nodeCount) {
        val f = (i + 0.5f) / nodeCount
        val taper = 0.22f + 0.78f * sin(f * PI.toFloat()).let { it * it }
        val stroke = maxWidth * taper
        val a = mirror(nodes[i])
        val b = mirror(nodes[i + 1])
        drawLine(ink.bark, a, b, strokeWidth = stroke, cap = StrokeCap.Round)
        val n = outwardNormal(f * length)
        val shift = mirror(nodes[i] + n * (stroke * 0.22f)) - a
        drawLine(
            ink.barkHighlight.copy(alpha = 0.45f),
            a + shift,
            b + shift,
            strokeWidth = stroke * 0.28f,
            cap = StrokeCap.Round,
        )
    }

    // Twigs, blossoms and buds placed along the branch. (position, size, rotation, tilt, alpha)
    data class Bloom(val at: Float, val sizeDp: Float, val spin: Float, val tilt: Float, val alpha: Float, val bud: Boolean = false, val twig: Float = 0f)
    val blooms = listOf(
        Bloom(at = 0.05f, sizeDp = 0f, spin = 0f, tilt = 1f, alpha = 1f, bud = true, twig = 5f),
        Bloom(at = 0.20f, sizeDp = 6.4f, spin = 18f, tilt = 0.74f, alpha = 1f, twig = 4f),
        Bloom(at = 0.36f, sizeDp = 9.2f, spin = -8f, tilt = 1f, alpha = 1f),
        Bloom(at = 0.47f, sizeDp = 5.4f, spin = 40f, tilt = 0.62f, alpha = 0.82f, twig = 6f),
        Bloom(at = 0.60f, sizeDp = 8.2f, spin = 26f, tilt = 0.86f, alpha = 1f),
        Bloom(at = 0.79f, sizeDp = 6.2f, spin = -30f, tilt = 0.7f, alpha = 1f, twig = 5f),
        Bloom(at = 0.95f, sizeDp = 0f, spin = 0f, tilt = 1f, alpha = 1f, bud = true, twig = 4f),
    )
    blooms.forEach { bloom ->
        val d = bloom.at * length
        val base = measure.getPosition(d)
        val n = outwardNormal(d)
        val tip = base + n * (bloom.twig * dp + bloom.sizeDp * 0.35f * dp)
        if (bloom.twig > 0f) {
            drawLine(ink.bark, mirror(base), mirror(tip), strokeWidth = 1.1f * dp, cap = StrokeCap.Round)
        }
        val centre = mirror(tip)
        if (bloom.bud) {
            val angle = atan2(n.y, if (mirrored) -n.x else n.x) * 180f / PI.toFloat()
            drawSumiBud(centre, 2.8f * dp, angle + 90f, ink)
        } else {
            drawSumiSakura(centre, bloom.sizeDp * dp, bloom.spin, bloom.tilt, ink, bloom.alpha)
        }
    }
}

/** One five-petal sakura with the characteristic notched petal tips. */
internal fun DrawScope.drawSumiSakura(
    centre: Offset,
    radius: Float,
    rotationDegrees: Float,
    tilt: Float,
    ink: SumiBlossomInk,
    alpha: Float = 1f,
) {
    if (radius <= 0f) return
    val petal = sakuraPetalPath(radius)
    val petalBrush = Brush.radialGradient(
        colors = listOf(ink.petalHeart.copy(alpha = alpha), ink.petalEdge.copy(alpha = alpha)),
        center = centre,
        radius = radius,
    )
    withTransform({
        rotate(rotationDegrees, centre)
        scale(1f, tilt, centre)
    }) {
        for (i in 0 until 5) {
            rotate(i * 72f, centre) {
                val p = Path().apply { addPath(petal, centre) }
                drawPath(p, petalBrush)
                drawPath(p, ink.petalLine.copy(alpha = ink.petalLine.alpha * alpha), style = Stroke(width = radius * 0.05f))
            }
        }
        // Stamens: fine lines with tiny anthers, then the deeper-blush eye.
        for (i in 0 until 7) {
            val a = (i * 51.4f + 12f) * PI.toFloat() / 180f
            val end = centre + Offset(cos(a), sin(a)) * (radius * 0.42f)
            drawLine(ink.stamen.copy(alpha = 0.75f * alpha), centre, end, strokeWidth = radius * 0.045f, cap = StrokeCap.Round)
            drawCircle(ink.stamen.copy(alpha = alpha), radius = radius * 0.055f, center = end)
        }
        drawCircle(ink.eye.copy(alpha = 0.55f * alpha), radius = radius * 0.16f, center = centre)
    }
}

/** Closed sakura bud: a soft blush teardrop held by a dark sepal. */
internal fun DrawScope.drawSumiBud(
    centre: Offset,
    radius: Float,
    rotationDegrees: Float,
    ink: SumiBlossomInk,
) {
    rotate(rotationDegrees, centre) {
        val bud = Path().apply {
            moveTo(centre.x, centre.y - radius * 1.5f)
            cubicTo(centre.x + radius * 1.1f, centre.y - radius * 0.6f, centre.x + radius * 0.8f, centre.y + radius * 0.7f, centre.x, centre.y + radius * 0.8f)
            cubicTo(centre.x - radius * 0.8f, centre.y + radius * 0.7f, centre.x - radius * 1.1f, centre.y - radius * 0.6f, centre.x, centre.y - radius * 1.5f)
            close()
        }
        drawPath(
            bud,
            Brush.verticalGradient(
                listOf(ink.petalEdge, ink.petalHeart),
                startY = centre.y - radius * 1.5f,
                endY = centre.y + radius,
            ),
        )
        val sepal = Path().apply {
            moveTo(centre.x - radius * 0.75f, centre.y + radius * 0.35f)
            quadraticTo(centre.x, centre.y + radius * 0.05f, centre.x + radius * 0.75f, centre.y + radius * 0.35f)
            lineTo(centre.x, centre.y + radius * 1.4f)
            close()
        }
        drawPath(sepal, ink.sepal)
    }
}

/** A single petal pointing up (negative y) from the origin, with a notch at its tip. */
private fun sakuraPetalPath(radius: Float): Path {
    val l = radius
    val wHalf = radius * 0.36f
    return Path().apply {
        moveTo(0f, 0f)
        cubicTo(-wHalf * 1.25f, -l * 0.28f, -wHalf * 1.45f, -l * 0.82f, -wHalf * 0.42f, -l * 0.99f)
        lineTo(0f, -l * 0.84f)
        lineTo(wHalf * 0.42f, -l * 0.99f)
        cubicTo(wHalf * 1.45f, -l * 0.82f, wHalf * 1.25f, -l * 0.28f, 0f, 0f)
        close()
    }
}

/**
 * A short ink sprig, used by the Sumi swatch in the theme picker and by the profile backdrop.
 * [start] is where the branch enters; it grows towards [end] with blossoms along the way.
 */
internal fun DrawScope.drawSumiSprig(
    start: Offset,
    end: Offset,
    thickness: Float,
    ink: SumiBlossomInk,
    blossomRadius: Float,
    seed: Int = 0,
) {
    val segments = 6
    val dir = end - start
    val len = dir.getDistance()
    if (len <= 0f) return
    val normal = Offset(-dir.y / len, dir.x / len)
    val points = (0..segments).map { i ->
        val f = i / segments.toFloat()
        val wobble = sin(f * 7.3f + seed) * len * 0.035f * (if (i == 0 || i == segments) 0f else 1f)
        start + dir * f + normal * wobble
    }
    for (i in 0 until segments) {
        val f = i / segments.toFloat()
        val stroke = thickness * (1f - 0.72f * f)
        drawLine(ink.bark, points[i], points[i + 1], strokeWidth = stroke, cap = StrokeCap.Round)
        drawLine(ink.barkHighlight.copy(alpha = 0.4f), points[i] - normal * stroke * 0.2f, points[i + 1] - normal * stroke * 0.2f, strokeWidth = stroke * 0.25f, cap = StrokeCap.Round)
    }
    // Two side twigs.
    listOf(2 to 1f, 4 to -1f).forEach { (index, side) ->
        val from = points[index]
        val to = from + (dir / len) * (len * 0.12f) + normal * (side * len * 0.16f)
        drawLine(ink.bark, from, to, strokeWidth = thickness * 0.35f, cap = StrokeCap.Round)
        drawSumiSakura(to, blossomRadius * 0.8f, 20f * side + seed * 11f, 0.78f, ink)
    }
    drawSumiSakura(points[3] + normal * blossomRadius * 0.4f, blossomRadius, -12f + seed * 7f, 1f, ink)
    drawSumiSakura(points[5] - normal * blossomRadius * 0.3f, blossomRadius * 0.72f, 34f, 0.66f, ink, alpha = 0.7f)
    val tipAngle = atan2(dir.y, dir.x) * 180f / PI.toFloat() + 90f
    drawSumiBud(points[segments], blossomRadius * 0.38f, tipAngle, ink)
}

/** Theme picker swatch for Sumi: an ink circle holding a small blossom sprig. */
@Composable
fun SumiThemeSwatch(
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit = {},
) {
    androidx.compose.foundation.layout.Box(
        modifier = modifier
            .clip(androidx.compose.foundation.shape.CircleShape)
            .background(ThemeColors.Sumi.background)
            .border(1.dp, ThemeColors.Sumi.secondaryVariant.copy(alpha = 0.45f), androidx.compose.foundation.shape.CircleShape)
            .drawWithCache {
                val ink = SumiBlossomColors.OnInk
                onDrawBehind {
                    // Fade the art a little when selected so the check mark stays readable.
                    val fadeAlpha = if (isSelected) 0.45f else 1f
                    drawContext.canvas.saveLayer(
                        androidx.compose.ui.geometry.Rect(Offset.Zero, size),
                        androidx.compose.ui.graphics.Paint().apply { alpha = fadeAlpha },
                    )
                    drawSumiSprig(
                        start = Offset(size.width * 0.96f, size.height * 0.18f),
                        end = Offset(size.width * 0.18f, size.height * 0.78f),
                        thickness = size.minDimension * 0.07f,
                        ink = ink,
                        blossomRadius = size.minDimension * 0.15f,
                    )
                    drawContext.canvas.restore()
                }
            },
        contentAlignment = androidx.compose.ui.Alignment.Center,
    ) {
        content()
    }
}

/**
 * "Who's watching" backdrop for Sumi: quiet ink night with one blossom branch reaching in from
 * the top-right corner and a few drifting petals, leaving the avatars and title on clean ink.
 * Drawn in code so the branch always hugs the real corner on phones, tablets and TVs.
 */
@Composable
fun SumiProfileBackdrop(modifier: Modifier = Modifier) {
    androidx.compose.foundation.Canvas(modifier = modifier) {
        drawRect(
            Brush.verticalGradient(
                listOf(Color(0xFF1A1618), Color(0xFF110F10), Color(0xFF0B0A0B)),
            ),
        )
        drawCircle(
            Brush.radialGradient(
                listOf(Color(0xFFC7B3B0).copy(alpha = 0.07f), Color.Transparent),
                center = Offset(size.width * 0.82f, size.height * 0.06f),
                radius = size.maxDimension * 0.55f,
            ),
            radius = size.maxDimension * 0.55f,
            center = Offset(size.width * 0.82f, size.height * 0.06f),
        )
        val unit = min(size.width, size.height)
        val ink = SumiBlossomColors.OnInk.copy(bark = Color(0xFF3E3335), barkHighlight = Color(0xFF7A6769))
        // Main branch from the top-right corner, kept within the top quarter of the screen.
        drawSumiSprig(
            start = Offset(size.width + unit * 0.02f, -unit * 0.02f),
            end = Offset(size.width - unit * 0.62f, min(size.height * 0.24f, unit * 0.5f)),
            thickness = unit * 0.022f,
            ink = ink,
            blossomRadius = unit * 0.042f,
            seed = 1,
        )
        // Small counter-sprig in the top-left for balance.
        drawSumiSprig(
            start = Offset(-unit * 0.02f, unit * 0.06f),
            end = Offset(unit * 0.24f, unit * 0.2f),
            thickness = unit * 0.011f,
            ink = ink,
            blossomRadius = unit * 0.024f,
            seed = 3,
        )
        // A few falling petals and faint snow.
        val petals = listOf(0.58f to 0.38f, 0.22f to 0.52f, 0.81f to 0.63f, 0.37f to 0.79f, 0.68f to 0.88f)
        petals.forEachIndexed { index, (fx, fy) ->
            drawSumiSakura(
                Offset(size.width * fx, size.height * fy),
                unit * 0.012f,
                rotationDegrees = index * 47f,
                tilt = 0.45f,
                ink = ink,
                alpha = 0.32f,
            )
        }
        val snow = listOf(0.12f to 0.3f, 0.44f to 0.18f, 0.9f to 0.42f, 0.3f to 0.66f, 0.74f to 0.74f, 0.52f to 0.95f, 0.08f to 0.86f)
        snow.forEach { (fx, fy) ->
            drawCircle(Color.White.copy(alpha = 0.10f), radius = unit * 0.004f, center = Offset(size.width * fx, size.height * fy))
        }
    }
}
