package com.smartir.remote.ui.components

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import com.smartir.remote.ir.SonyCodes
import com.smartir.remote.ui.viewmodel.RemoteViewModel

/**
 * Wraps content and detects edge swipes for volume (right edge).
 * Gesture handler is on the parent Box so child buttons receive touches normally.
 */
@Composable
fun SwipeGestureOverlay(
    viewModel: RemoteViewModel,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    val view = LocalView.current
    val thresholdPx = with(density) { 40.dp.toPx() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                val edgeZoneWidth = size.width * 0.15f

                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val startX = down.position.x

                    val isRightEdge = startX > size.width - edgeZoneWidth

                    if (!isRightEdge) {
                        // Not in edge zone — let buttons handle it
                        return@awaitEachGesture
                    }

                    down.consume()

                    var accumulatedDrag = 0f

                    do {
                        val event = awaitPointerEvent()

                        if (event.type == PointerEventType.Move) {
                            val change = event.changes.firstOrNull() ?: continue
                            val deltaY = change.position.y - change.previousPosition.y
                            accumulatedDrag += deltaY
                            change.consume()

                            while (accumulatedDrag >= thresholdPx) {
                                accumulatedDrag -= thresholdPx
                                viewModel.sendCommand(SonyCodes.VOLUME_DOWN, view)
                            }
                            while (accumulatedDrag <= -thresholdPx) {
                                accumulatedDrag += thresholdPx
                                viewModel.sendCommand(SonyCodes.VOLUME_UP, view)
                            }
                        }
                    } while (event.changes.any { it.pressed })
                }
            }
    ) {
        content()
    }
}