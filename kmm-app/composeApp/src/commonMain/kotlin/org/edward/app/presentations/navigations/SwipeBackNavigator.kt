package org.edward.app.presentations.navigations

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.Navigator
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private val EDGE_THRESHOLD = 32.dp
private const val POP_THRESHOLD_FRACTION = 0.35f
private const val BEHIND_SCREEN_PARALLAX = 0.3f

@Composable
fun SwipeBackContent(navigator: Navigator) {
    val canGoBack = navigator.canPop
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val edgeThresholdPx = with(density) { EDGE_THRESHOLD.toPx() }

    val offsetX = remember { Animatable(0f) }
    var containerWidth by remember { mutableStateOf(1f) }
    var isDragging by remember { mutableStateOf(false) }
    var startedFromEdge by remember { mutableStateOf(false) }

    val currentScreenKey = navigator.lastItem.key
    LaunchedEffect(currentScreenKey) {
        offsetX.snapTo(0f)
        isDragging = false
        startedFromEdge = false
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { containerWidth = it.width.toFloat().coerceAtLeast(1f) }
    ) {
        val swipeProgress = (offsetX.value / containerWidth).coerceIn(0f, 1f)
        val isSwiping = isDragging && canGoBack && offsetX.value > 0f

        // Previous screen (behind), with parallax offset
        if (isSwiping && navigator.items.size >= 2) {
            val previousScreen = navigator.items[navigator.items.size - 2]
            val parallaxOffset = -containerWidth * BEHIND_SCREEN_PARALLAX * (1f - swipeProgress)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .offset { IntOffset(parallaxOffset.roundToInt(), 0) }
            ) {
                previousScreen.Content()
            }
            // Dimming overlay that fades as the user swipes
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.15f * (1f - swipeProgress)))
            )
        }

        // Current screen with drag-to-dismiss + edge shadow during swipe
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .then(
                    if (isSwiping) {
                        Modifier.drawWithContent {
                            drawContent()
                            // Left edge shadow to give depth illusion
                            drawRect(
                                color = Color.Black.copy(alpha = 0.08f * (1f - swipeProgress)),
                                size = size.copy(width = 4.dp.toPx())
                            )
                        }
                    } else Modifier
                )
                .pointerInput(canGoBack, currentScreenKey) {
                    if (!canGoBack) return@pointerInput
                    detectHorizontalDragGestures(
                        onDragStart = { startPosition ->
                            startedFromEdge = startPosition.x < edgeThresholdPx
                            isDragging = startedFromEdge
                        },
                        onDragEnd = {
                            if (!startedFromEdge) return@detectHorizontalDragGestures
                            isDragging = false
                            if (offsetX.value > containerWidth * POP_THRESHOLD_FRACTION) {
                                scope.launch {
                                    offsetX.animateTo(containerWidth, tween(220))
                                    navigator.pop()
                                }
                            } else {
                                scope.launch {
                                    offsetX.animateTo(0f, tween(220))
                                }
                            }
                            startedFromEdge = false
                        },
                        onDragCancel = {
                            if (!startedFromEdge) return@detectHorizontalDragGestures
                            isDragging = false
                            startedFromEdge = false
                            scope.launch { offsetX.animateTo(0f, tween(220)) }
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            if (!startedFromEdge) return@detectHorizontalDragGestures
                            val newX = (offsetX.value + dragAmount).coerceIn(0f, containerWidth)
                            scope.launch { offsetX.snapTo(newX) }
                        }
                    )
                }
        ) {
            navigator.lastItem.Content()
        }
    }
}
