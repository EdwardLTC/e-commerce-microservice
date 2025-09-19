package org.edward.app.presentations.screens.components

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun SwipeBackHandler(
    enabled: Boolean = true,
    edgeWidth: Dp = 32.dp,
    threadHold: Float = 2f,
    onSwipeBack: () -> Unit,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    val edgeWidthPx = with(density) { edgeWidth.toPx() }
    var dragX by remember { mutableStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (dragX > edgeWidthPx * threadHold) {
                            onSwipeBack()
                        }
                        dragX = 0f
                    }
                ) { change, dragAmount ->
                    if (change.position.x < edgeWidthPx) {
                        dragX += dragAmount
                    }
                }
            }
    ) {
        content()
    }
}
