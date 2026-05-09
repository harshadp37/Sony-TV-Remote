package com.smartir.remote.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.smartir.remote.haptics.HapticManager
import com.smartir.remote.ui.theme.ButtonDefault
import com.smartir.remote.ui.theme.ButtonPressed

@Composable
fun AdbButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    label: String? = null,
    enabled: Boolean = true,
    size: Dp = 56.dp,
    width: Dp? = null,
    backgroundColor: Color = ButtonDefault,
    gradientEndColor: Color? = null,
    pressedColor: Color = ButtonPressed,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    disabledColor: Color = Color(0xFF1A1A2A),
    disabledContentColor: Color = Color(0xFF555566),
    iconSize: Dp = 26.dp,
    shadowColor: Color = Color.Black,
    elevation: Dp = 10.dp,
    contentDescription: String? = label
) {
    var isPressed by remember { mutableStateOf(false) }
    val view = LocalView.current
    val shape = RoundedCornerShape(20.dp)

    val fgColor = if (enabled) contentColor else disabledContentColor

    val sizeModifier = if (width != null) Modifier.width(width).height(size) else Modifier.size(size)

    val bgModifier = when {
        !enabled -> Modifier.background(disabledColor)
        isPressed -> Modifier.background(pressedColor)
        gradientEndColor != null -> Modifier.background(
            Brush.linearGradient(
                colors = listOf(backgroundColor, gradientEndColor),
                start = Offset.Zero,
                end = Offset.Infinite
            )
        )
        else -> Modifier.background(backgroundColor)
    }

    Box(
        modifier = modifier
            .shadow(
                elevation = if (isPressed || !enabled) 4.dp else elevation,
                shape = shape,
                ambientColor = shadowColor.copy(alpha = 0.6f),
                spotColor = shadowColor.copy(alpha = 0.6f)
            )
            .then(sizeModifier)
            .clip(shape)
            .then(bgModifier)
            .then(
                if (enabled) {
                    Modifier.pointerInput(Unit) {
                        awaitEachGesture {
                            awaitFirstDown(requireUnconsumed = false)
                            isPressed = true
                            HapticManager.tick(view)
                            onClick()
                            try {
                                do {
                                    val event = awaitPointerEvent()
                                } while (event.changes.any { it.pressed })
                            } finally {
                                isPressed = false
                            }
                        }
                    }
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = fgColor,
                modifier = Modifier.size(iconSize)
            )
        } else if (label != null) {
            Text(
                text = label,
                color = fgColor,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
