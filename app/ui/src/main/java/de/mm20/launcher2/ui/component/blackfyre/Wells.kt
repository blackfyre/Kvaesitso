package de.mm20.launcher2.ui.component.blackfyre

import android.graphics.BlurMaskFilter
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.addOutline
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import de.mm20.launcher2.ui.theme.blackfyre.LocalBlackfyreDepth
import de.mm20.launcher2.ui.theme.blackfyre.blurMaskRadius
import de.mm20.launcher2.ui.theme.typography.fontfamily.bundledFontFamily

/** bf-ink-code: the well ground (Paper and Dark). */
private val WellPaper = Color(0xFF121211)
private val WellDark = Color(0xFF0B0B0A)
/** bf-on-panel: content in a well. */
private val OnWell = Color(0xFFF3F2EE)
private val WellShape = RoundedCornerShape(10.dp) // bf-radius-lg
private val WellPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)

/**
 * Fork (blackfyre-components): content set into its panel as a well (bf-depth-well:
 * `inset 0 2px 5px rgba(0,0,0,.55), inset 0 0 0 1px #000` on `bf-ink-code`), in light ink and
 * JetBrains Mono with tabular figures, like the site's stat tiles. Wells stay lit from above.
 * Without Blackfyre this is a plain [Box] with [modifier]: [margin] and the well's padding apply
 * only under Blackfyre, so upstream's layout is unchanged.
 */
@Composable
fun BlackfyreWell(
    modifier: Modifier = Modifier,
    margin: PaddingValues = PaddingValues(0.dp),
    content: @Composable BoxScope.() -> Unit,
) {
    val depth = LocalBlackfyreDepth.current
    if (depth == null) {
        Box(modifier, content = content)
        return
    }
    Box(
        modifier = modifier
            .padding(margin)
            .blackfyreWell(WellShape, if (depth.dark) WellDark else WellPaper)
            .padding(WellPadding),
    ) {
        CompositionLocalProvider(LocalContentColor provides OnWell) {
            MaterialTheme(typography = MaterialTheme.typography.monoNumbers()) {
                content()
            }
        }
    }
}

private fun Modifier.blackfyreWell(shape: Shape, ground: Color): Modifier = this
    .clip(shape)
    .background(ground)
    .drawWithCache {
        val path = Path().apply { addOutline(shape.createOutline(size, layoutDirection, this@drawWithCache)) }
        // The inset shadow: everything outside the shape moved 2 dp down, blurred, inside the shape.
        val frame = android.graphics.Path().apply {
            val pad = 20.dp.toPx()
            addRect(-pad, -pad, size.width + pad, size.height + pad, android.graphics.Path.Direction.CW)
            val shifted = android.graphics.Path(path.asAndroidPath()).apply { offset(0f, 2.dp.toPx()) }
            op(shifted, android.graphics.Path.Op.DIFFERENCE)
        }
        val shadow = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.Black.copy(alpha = 0.55f).toArgb()
            maskFilter = BlurMaskFilter(blurMaskRadius(5.dp.toPx()), BlurMaskFilter.Blur.NORMAL)
        }
        val ring = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.BLACK
            style = android.graphics.Paint.Style.STROKE
            strokeWidth = 2.dp.toPx() // centred on the outline: 1 dp falls inside the clip
        }
        onDrawWithContent {
            drawContent()
            clipPath(path) {
                drawIntoCanvas { canvas ->
                    canvas.nativeCanvas.drawPath(frame, shadow)
                    canvas.nativeCanvas.drawPath(path.asAndroidPath(), ring)
                }
            }
        }
    }

/** JetBrains Mono, with tabular figures. */
internal fun monoNumbers(style: TextStyle): TextStyle =
    style.copy(fontFamily = bundledFontFamily("jetbrains_mono", emptyMap()), fontFeatureSettings = "tnum")

/** This typography with every style in [monoNumbers]. */
fun Typography.monoNumbers(): Typography = copy(
    displayLarge = monoNumbers(displayLarge), displayMedium = monoNumbers(displayMedium),
    displaySmall = monoNumbers(displaySmall), headlineLarge = monoNumbers(headlineLarge),
    headlineMedium = monoNumbers(headlineMedium), headlineSmall = monoNumbers(headlineSmall),
    titleLarge = monoNumbers(titleLarge), titleMedium = monoNumbers(titleMedium),
    titleSmall = monoNumbers(titleSmall), bodyLarge = monoNumbers(bodyLarge),
    bodyMedium = monoNumbers(bodyMedium), bodySmall = monoNumbers(bodySmall),
    labelLarge = monoNumbers(labelLarge), labelMedium = monoNumbers(labelMedium),
    labelSmall = monoNumbers(labelSmall),
)

/** This typography with its display styles in [monoNumbers] (digital clock faces). */
fun Typography.monoDisplay(): Typography = copy(
    displayLarge = monoNumbers(displayLarge),
    displayMedium = monoNumbers(displayMedium),
    displaySmall = monoNumbers(displaySmall),
)
