package com.smartir.remote.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.smartir.remote.ir.SonyIrCommand
import com.smartir.remote.ui.theme.ButtonDefault
import com.smartir.remote.ui.theme.ButtonPressed
import com.smartir.remote.ui.viewmodel.RemoteViewModel

@Composable
fun RemoteButton(
    command: SonyIrCommand,
    viewModel: RemoteViewModel,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    label: String? = null,
    holdToRepeat: Boolean = false,
    size: Dp = 60.dp,
    width: Dp? = null,
    height: Dp? = null,
    backgroundColor: Color = ButtonDefault,
    pressedColor: Color = ButtonPressed,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    iconSize: Dp = 26.dp,
    shadowColor: Color = Color.Black,
    elevation: Dp = 10.dp,
    onSent: (() -> Unit)? = null
) {
    var isPressed by remember { mutableStateOf(false) }
    val view = LocalView.current
    val shape = RoundedCornerShape(20.dp)

    Box(
        modifier = modifier
            .shadow(
                elevation = if (isPressed) 4.dp else elevation,
                shape = shape,
                ambientColor = shadowColor.copy(alpha = 0.6f),
                spotColor = shadowColor.copy(alpha = 0.6f)
            )
            .then(
                if (width != null || height != null) {
                    Modifier
                        .width(width ?: size)
                        .height(height ?: size)
                } else {
                    Modifier.size(size)
                }
            )
            .clip(shape)
            .background(if (isPressed) pressedColor else backgroundColor)
            .pointerInput(command, holdToRepeat) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    isPressed = true

                    if (holdToRepeat) {
                        viewModel.startRepeat(command, view)
                    } else {
                        viewModel.sendCommand(command, view)
                        onSent?.invoke()
                    }

                    try {
                        do {
                            val event = awaitPointerEvent()
                        } while (event.changes.any { it.pressed })
                    } finally {
                        isPressed = false
                        if (holdToRepeat) {
                            viewModel.stopRepeat()
                        }
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = command.name,
                tint = contentColor,
                modifier = Modifier.size(iconSize)
            )
        } else if (label != null) {
            Text(
                text = label,
                color = contentColor,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
