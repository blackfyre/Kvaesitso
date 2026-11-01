package de.mm20.launcher2.ui.component.blackfyre

import android.animation.ValueAnimator
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.addOutline
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** The keycap edge's height (design system: 2px for light and orange keys). */
internal val KeyEdge = 2.dp
private val KeyHighlight = 1.dp

/** How a key reacts to taps: a plain click, or a toggle with its current value. */
internal sealed interface KeyAction {
    data class Click(val onClick: () -> Unit, val role: Role = Role.Button) : KeyAction
    data class Toggle(val value: Boolean, val onValueChange: (Boolean) -> Unit, val role: Role = Role.Checkbox) : KeyAction
}

/**
 * Fork (blackfyre-components): a keycap. The face sits on an edge of [KeyEdge] below it and has a
 * 1 dp highlight on top. While pressed, or when [held] (a selected chip, a checked toggle), the
 * face sits lowered over the edge and has no highlight. Disabled keys draw at 38% without an edge.
 * The layout reserves the edge below the face, so pressing never moves anything else.
 */
@Composable
internal fun Key(
    action: KeyAction,
    modifier: Modifier,
    enabled: Boolean,
    palette: KeyPalette,
    held: Boolean,
    cornerRadius: Dp,
    minWidth: Dp,
    minHeight: Dp,
    contentPadding: PaddingValues,
    textStyle: TextStyle,
    interactionSource: MutableInteractionSource?,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Center,
    content: @Composable RowScope.() -> Unit,
) {
    val interactions = interactionSource ?: remember { MutableInteractionSource() }
    val pressed by interactions.collectIsPressedAsState()
    val down = enabled && (held || pressed)
    val drop by animateDpAsState(
        targetValue = if (down) KeyEdge else 0.dp,
        animationSpec = if (ValueAnimator.areAnimatorsEnabled()) tween(50) else snap(),
        label = "keyDrop",
    )
    val shape = RoundedCornerShape(cornerRadius)
    val input = when (action) {
        is KeyAction.Click -> Modifier.clickable(
            interactionSource = interactions,
            indication = null,
            enabled = enabled,
            role = action.role,
            onClick = action.onClick,
        )

        is KeyAction.Toggle -> Modifier.toggleable(
            value = action.value,
            interactionSource = interactions,
            indication = null,
            enabled = enabled,
            role = action.role,
            onValueChange = action.onValueChange,
        )
    }
    Box(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .semantics(mergeDescendants = true) {
                role = if (action is KeyAction.Toggle) action.role else (action as KeyAction.Click).role
            }
            .then(input)
            .alpha(if (enabled) 1f else 0.38f)
            .padding(bottom = KeyEdge)
            .drawBehind {
                // The edge: the face's shape, [KeyEdge] lower. A lowered face covers it.
                if (!enabled) return@drawBehind
                val outline = shape.createOutline(size, layoutDirection, this)
                translate(top = KeyEdge.toPx()) {
                    drawOutline(outline, palette.edge)
                }
            },
        propagateMinConstraints = true,
    ) {
        Row(
            modifier = Modifier
                .graphicsLayer { translationY = drop.toPx() }
                .clip(shape)
                .background(palette.face)
                .drawWithContent {
                    drawContent()
                    val highlight = palette.highlight ?: return@drawWithContent
                    if (down) return@drawWithContent
                    val path = Path().apply { addOutline(shape.createOutline(size, layoutDirection, this@drawWithContent)) }
                    val shifted = Path().apply { addPath(path, Offset(0f, KeyHighlight.toPx())) }
                    clipPath(shifted, ClipOp.Difference) { drawRect(highlight) }
                }
                .defaultMinSize(minWidth, minHeight)
                .padding(contentPadding),
            horizontalArrangement = horizontalArrangement,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CompositionLocalProvider(
                LocalContentColor provides palette.content,
                LocalTextStyle provides LocalTextStyle.current.merge(textStyle),
            ) {
                content()
            }
        }
    }
}
