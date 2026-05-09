package com.smartir.remote.ui.components

import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import com.smartir.remote.ir.SonyCodes
import com.smartir.remote.ir.SonyIrCommand
import com.smartir.remote.ui.viewmodel.RemoteViewModel
import kotlin.math.abs

private enum class DPadZone { UP, DOWN, LEFT, RIGHT, OK, NONE }

private val ZoneColor = Color(0xFF1E1E30)
private val ZonePressed = Color(0xFF2E2E48)
private val DividerColor = Color(0xFF0A0A14)
private val ArrowColor = Color(0xFF8888BB)
private val OkColor = Color(0xFF3366CC)
private val OkPressedColor = Color(0xFF2855AA)

@Composable
fun SegmentedDPad(
    viewModel: RemoteViewModel,
    modifier: Modifier = Modifier
) {
    var pressedZone by remember { mutableStateOf(DPadZone.NONE) }
    val view = LocalView.current

    Canvas(
        modifier = modifier
            .size(260.dp)
            .shadow(
                elevation = 16.dp,
                shape = CircleShape,
                ambientColor = Color.Black.copy(alpha = 0.6f),
                spotColor = Color.Black.copy(alpha = 0.6f)
            )
            .clip(CircleShape)
            .pointerInput(Unit) {
                val cx = size.width / 2f
                val cy = size.height / 2f
                val radius = size.width / 2f
                val squareHalf = size.width * 22.5f / 130f

                fun getZone(pos: Offset): DPadZone {
                    val px = pos.x - cx
                    val py = pos.y - cy
                    if (px * px + py * py > radius * radius) return DPadZone.NONE
                    if (abs(px) <= squareHalf && abs(py) <= squareHalf) return DPadZone.OK
                    return when {
                        py < 0 && abs(py) > abs(px) -> DPadZone.UP
                        py > 0 && abs(py) > abs(px) -> DPadZone.DOWN
                        px < 0 && abs(px) > abs(py) -> DPadZone.LEFT
                        px > 0 && abs(px) > abs(py) -> DPadZone.RIGHT
                        else -> DPadZone.NONE
                    }
                }

                fun zoneToCommand(zone: DPadZone): SonyIrCommand? = when (zone) {
                    DPadZone.UP -> SonyCodes.DPAD_UP
                    DPadZone.DOWN -> SonyCodes.DPAD_DOWN
                    DPadZone.LEFT -> SonyCodes.DPAD_LEFT
                    DPadZone.RIGHT -> SonyCodes.DPAD_RIGHT
                    DPadZone.OK -> SonyCodes.DPAD_OK
                    DPadZone.NONE -> null
                }

                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val zone = getZone(down.position)
                    if (zone == DPadZone.NONE) return@awaitEachGesture

                    pressedZone = zone
                    val command = zoneToCommand(zone) ?: return@awaitEachGesture
                    val isDirectional = zone != DPadZone.OK

                    if (isDirectional) {
                        viewModel.startRepeat(command, view)
                    } else {
                        viewModel.sendCommand(command, view)
                    }

                    try {
                        do {
                            val event = awaitPointerEvent()
                        } while (event.changes.any { it.pressed })
                    } finally {
                        if (isDirectional) {
                            viewModel.stopRepeat()
                        }
                        pressedZone = DPadZone.NONE
                    }
                }
            }
    ) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val r = size.width / 2f
        val sh = size.width * 22.5f / 130f
        val cos45 = 0.7071f
        val circleRect = Rect(0f, 0f, size.width, size.height)

        // Circle intersection points at 45° diagonals
        val ciTL = Offset(cx - r * cos45, cy - r * cos45)
        val ciTR = Offset(cx + r * cos45, cy - r * cos45)
        val ciBR = Offset(cx + r * cos45, cy + r * cos45)
        val ciBL = Offset(cx - r * cos45, cy + r * cos45)

        // Square corners
        val sqTL = Offset(cx - sh, cy - sh)
        val sqTR = Offset(cx + sh, cy - sh)
        val sqBR = Offset(cx + sh, cy + sh)
        val sqBL = Offset(cx - sh, cy + sh)

        // UP zone
        val upPath = Path().apply {
            moveTo(sqTL.x, sqTL.y)
            lineTo(ciTL.x, ciTL.y)
            arcTo(circleRect, 225f, 90f, false)
            lineTo(sqTR.x, sqTR.y)
            close()
        }
        drawPath(upPath, if (pressedZone == DPadZone.UP) ZonePressed else ZoneColor)

        // RIGHT zone
        val rightPath = Path().apply {
            moveTo(sqTR.x, sqTR.y)
            lineTo(ciTR.x, ciTR.y)
            arcTo(circleRect, 315f, 90f, false)
            lineTo(sqBR.x, sqBR.y)
            close()
        }
        drawPath(rightPath, if (pressedZone == DPadZone.RIGHT) ZonePressed else ZoneColor)

        // DOWN zone
        val downPath = Path().apply {
            moveTo(sqBR.x, sqBR.y)
            lineTo(ciBR.x, ciBR.y)
            arcTo(circleRect, 45f, 90f, false)
            lineTo(sqBL.x, sqBL.y)
            close()
        }
        drawPath(downPath, if (pressedZone == DPadZone.DOWN) ZonePressed else ZoneColor)

        // LEFT zone
        val leftPath = Path().apply {
            moveTo(sqBL.x, sqBL.y)
            lineTo(ciBL.x, ciBL.y)
            arcTo(circleRect, 135f, 90f, false)
            lineTo(sqTL.x, sqTL.y)
            close()
        }
        drawPath(leftPath, if (pressedZone == DPadZone.LEFT) ZonePressed else ZoneColor)

        // Divider lines from square corners to circle edge
        val dividerWidth = 2.dp.toPx()
        drawLine(DividerColor, sqTL, ciTL, dividerWidth)
        drawLine(DividerColor, sqTR, ciTR, dividerWidth)
        drawLine(DividerColor, sqBR, ciBR, dividerWidth)
        drawLine(DividerColor, sqBL, ciBL, dividerWidth)

        // Arrow indicators
        val arrowSize = 8.dp.toPx()
        val arrowInset = 20.dp.toPx()

        // Up arrow
        val upArrow = Path().apply {
            moveTo(cx, arrowInset)
            lineTo(cx - arrowSize, arrowInset + arrowSize * 1.5f)
            lineTo(cx + arrowSize, arrowInset + arrowSize * 1.5f)
            close()
        }
        drawPath(upArrow, ArrowColor)

        // Down arrow
        val downY = size.height - arrowInset
        val downArrow = Path().apply {
            moveTo(cx, downY)
            lineTo(cx - arrowSize, downY - arrowSize * 1.5f)
            lineTo(cx + arrowSize, downY - arrowSize * 1.5f)
            close()
        }
        drawPath(downArrow, ArrowColor)

        // Left arrow
        val leftArrow = Path().apply {
            moveTo(arrowInset, cy)
            lineTo(arrowInset + arrowSize * 1.5f, cy - arrowSize)
            lineTo(arrowInset + arrowSize * 1.5f, cy + arrowSize)
            close()
        }
        drawPath(leftArrow, ArrowColor)

        // Right arrow
        val rightX = size.width - arrowInset
        val rightArrow = Path().apply {
            moveTo(rightX, cy)
            lineTo(rightX - arrowSize * 1.5f, cy - arrowSize)
            lineTo(rightX - arrowSize * 1.5f, cy + arrowSize)
            close()
        }
        drawPath(rightArrow, ArrowColor)

        // OK square button (rounded rect)
        drawRoundRect(
            color = if (pressedZone == DPadZone.OK) OkPressedColor else OkColor,
            topLeft = Offset(cx - sh, cy - sh),
            size = Size(sh * 2, sh * 2),
            cornerRadius = CornerRadius(10.dp.toPx())
        )

        // "OK" text
        drawContext.canvas.nativeCanvas.drawText(
            "OK",
            cx,
            cy + 7.dp.toPx(),
            android.graphics.Paint().apply {
                color = android.graphics.Color.WHITE
                textSize = 18.dp.toPx()
                textAlign = android.graphics.Paint.Align.CENTER
                typeface = Typeface.DEFAULT_BOLD
                isAntiAlias = true
            }
        )
    }
}
