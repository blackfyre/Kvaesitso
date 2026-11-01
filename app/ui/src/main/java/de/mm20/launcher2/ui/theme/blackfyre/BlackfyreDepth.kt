package de.mm20.launcher2.ui.theme.blackfyre

import android.graphics.BlurMaskFilter
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.addOutline
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.StateFlow

/**
 * Fork (blackfyre-depth): the colours of bf-depth-panel (design system tokens.json, version 97):
 * `inset 0 1px 0 <highlight>, 0 1px 2px <contact>, 0 10px 30px <cast>`.
 */
@Immutable
data class PanelDepth(val highlight: Color, val contact: Color, val cast: Color)

/** bf-depth-panel in Paper: `inset 0 1px 0 #fff, 0 1px 2px rgba(40,36,30,.08), 0 10px 30px rgba(40,36,30,.08)`. */
val PaperPanelDepth = PanelDepth(
    highlight = Color(0xFFFFFFFF), // bf-highlight
    contact = Color(40, 36, 30).copy(alpha = 0.08f),
    cast = Color(40, 36, 30).copy(alpha = 0.08f),
)

/** bf-depth-panel in Dark: `inset 0 1px 0 #33332f, 0 1px 2px rgba(0,0,0,.4), 0 10px 30px rgba(0,0,0,.35)`. */
val DarkPanelDepth = PanelDepth(
    highlight = Color(0xFF33332F), // bf-highlight
    contact = Color.Black.copy(alpha = 0.4f),
    cast = Color.Black.copy(alpha = 0.35f),
)

/** Depth while the Blackfyre colour theme is active: the panel colours and the light. */
class BlackfyreDepth(val panel: PanelDepth, val light: StateFlow<Light>)

/** Null unless the Blackfyre colour theme is active. */
val LocalBlackfyreDepth = staticCompositionLocalOf<BlackfyreDepth?> { null }

/** Provides [LocalBlackfyreDepth] when [enabled]; the light is created only then. */
@Composable
fun ProvideBlackfyreDepth(enabled: Boolean, dark: Boolean, content: @Composable () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val light = remember(context, enabled) { if (enabled) BlackfyreLight(context, scope) else null }
    val depth = remember(light, dark) {
        light?.let { BlackfyreDepth(if (dark) DarkPanelDepth else PaperPanelDepth, it.light) }
    }
    // One call site whatever [enabled] is, so switching themes keeps the content's state.
    CompositionLocalProvider(LocalBlackfyreDepth provides depth, content = content)
}

/** Material elevation to use: none while Blackfyre depth replaces it, otherwise [default]. */
@Composable
fun blackfyreElevation(default: Dp): Dp = if (LocalBlackfyreDepth.current != null) 0.dp else default

/** The cast shadow's offset for [light] (site: `calc(cx * 18px) calc(10px + cy * 9px)`). */
fun panelCastOffset(light: Light): DpOffset = DpOffset(18.dp * light.cx, 10.dp + 9.dp * light.cy)

/**
 * The [BlurMaskFilter] radius that matches a CSS box-shadow blur of [cssBlur]: CSS blurs with a
 * standard deviation of half the blur, Android with 0.57735 × radius + 0.5.
 */
fun blurMaskRadius(cssBlur: Float): Float = ((cssBlur / 2f - 0.5f) / 0.57735f).coerceAtLeast(0.5f)

private val ContactOffset = 1.dp
private val ContactBlur = 2.dp
private val CastBlur = 30.dp
private val HighlightHeight = 1.dp

/**
 * Fork (blackfyre-depth): draws a raised panel in [shape] while Blackfyre depth is active: a
 * contact and a cast shadow, outside the shape only, so that none shows through a translucent
 * background, and a highlight on the top inner edge. The light is read at draw time, so a change
 * of tilt redraws without recomposing. Without Blackfyre depth this is a no-op.
 */
@Composable
fun Modifier.blackfyrePanel(shape: Shape): Modifier {
    val depth = LocalBlackfyreDepth.current ?: return this
    val light = depth.light.collectAsStateWithLifecycle(minActiveState = Lifecycle.State.RESUMED)
    val colors = depth.panel
    return this.drawWithCache {
        val path = Path().apply { addOutline(shape.createOutline(size, layoutDirection, this@drawWithCache)) }
        val shifted = Path().apply { addPath(path, Offset(0f, HighlightHeight.toPx())) }
        val contact = shadowPaint(colors.contact, ContactBlur.toPx())
        val cast = shadowPaint(colors.cast, CastBlur.toPx())
        onDrawWithContent {
            val offset = panelCastOffset(light.value)
            clipPath(path, ClipOp.Difference) {
                drawShadow(path, contact, Offset(0f, ContactOffset.toPx()))
                drawShadow(path, cast, Offset(offset.x.toPx(), offset.y.toPx()))
            }
            drawContent()
            clipPath(path) {
                clipPath(shifted, ClipOp.Difference) { drawRect(colors.highlight) }
            }
        }
    }
}

private fun shadowPaint(color: Color, cssBlur: Float) =
    android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color.toArgb()
        maskFilter = BlurMaskFilter(blurMaskRadius(cssBlur), BlurMaskFilter.Blur.NORMAL)
    }

private fun DrawScope.drawShadow(path: Path, paint: android.graphics.Paint, offset: Offset) {
    drawIntoCanvas { canvas ->
        val native = canvas.nativeCanvas
        native.save()
        native.translate(offset.x, offset.y)
        native.drawPath(path.asAndroidPath(), paint)
        native.restore()
    }
}
