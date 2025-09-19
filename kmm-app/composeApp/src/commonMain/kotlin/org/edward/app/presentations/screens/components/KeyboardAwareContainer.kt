package org.edward.app.presentations.screens.components

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager

@Composable
fun KeyboardAwareContainer(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val focusManager = LocalFocusManager.current

    Box(
        modifier = modifier.fillMaxSize().pointerInput(Unit) {
            detectTapGestures {
                focusManager.clearFocus()
            }
        },
        content = content
    )
}