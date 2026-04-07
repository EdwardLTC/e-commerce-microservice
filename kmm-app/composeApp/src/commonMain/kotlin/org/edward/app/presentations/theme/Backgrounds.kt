package org.edward.app.presentations.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Brush

@Composable
fun rememberScreenGradientBrush(): Brush {
    val scheme = MaterialTheme.colorScheme
    return remember(scheme) {
        Brush.verticalGradient(
            colors = listOf(
                scheme.primary.copy(alpha = 0.12f),
                scheme.tertiary.copy(alpha = 0.08f),
                scheme.background
            )
        )
    }
}

