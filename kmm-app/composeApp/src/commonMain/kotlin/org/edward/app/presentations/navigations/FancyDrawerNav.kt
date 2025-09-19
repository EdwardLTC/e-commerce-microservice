package org.edward.app.presentations.navigations

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.tab.CurrentTab
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import kotlinx.coroutines.launch
import org.edward.app.presentations.screens.main.home.HomeScreen
import org.edward.app.presentations.screens.main.profile.ProfileScreen
import kotlin.math.roundToInt

class FancyDrawerNav(private val firstScreen: Tab = HomeScreen()) : Screen {

    @Composable
    override fun Content() {
        TabNavigator(firstScreen) {
            SlidingRootDrawer(
                drawerContent = { closeDrawer ->
                    DrawerTabItem(firstScreen, LocalTabNavigator.current, closeDrawer)
                    DrawerTabItem(ProfileScreen(), LocalTabNavigator.current, closeDrawer)
                }
            ) {
                CurrentTab()
            }
        }
    }
}

@Composable
fun DrawerTabItem(tab: Tab, tabNavigator: TabNavigator, closeDrawer: () -> Unit) {
    val selected = tabNavigator.current == tab
    if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface

    NavigationDrawerItem(
        label = { Text(tab.options.title) },
        selected = selected,
        onClick = {
            tabNavigator.current = tab
            closeDrawer.invoke()
        },
        icon = {
            tab.options.icon?.let {
                Icon(painter = it, contentDescription = tab.options.title)
            }
        },
        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
    )
}

@Composable
fun SlidingRootDrawer(
    drawerContent: @Composable ColumnScope.(closeDrawer: () -> Unit) -> Unit,
    modifier: Modifier = Modifier,
    drawerWidth: Dp = 280.dp,
    contentScale: Float = 0.8f,
    elevation: Dp = 12.dp,
    content: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()
    val drawerWidthPx = with(LocalDensity.current) { drawerWidth.toPx() }

    val offsetX = remember { Animatable(0f) }
    offsetX.value > drawerWidthPx / 2f

    fun openDrawer() =
        scope.launch { offsetX.animateTo(drawerWidthPx, tween(durationMillis = 250)) }

    fun closeDrawer() = scope.launch { offsetX.animateTo(0f, tween(durationMillis = 250)) }

    Box(modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    if (offsetX.value > 0f) {
                        closeDrawer()
                    } else {
                        openDrawer()
                    }
                }
                .width(drawerWidth)
                .background(MaterialTheme.colorScheme.background, RoundedCornerShape(12.dp)),
            content = { drawerContent { closeDrawer() } }
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .graphicsLayer {
                    val progress = offsetX.value / drawerWidthPx
                    scaleX = 1f - (1f - contentScale) * progress
                    scaleY = 1f - (1f - contentScale) * progress
                }
                .shadow(elevation)
                .background(MaterialTheme.colorScheme.background, RoundedCornerShape(12.dp))
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            val stickThreshold = drawerWidthPx * 0.25f
                            if (offsetX.value > drawerWidthPx / stickThreshold) {
                                openDrawer()
                            } else {
                                closeDrawer()
                            }
                        }
                    ) { change, dragAmount ->
                        val newX = (offsetX.value + dragAmount).coerceIn(0f, drawerWidthPx)
                        scope.launch { offsetX.snapTo(newX) }

                        if (dragAmount < -20) {
                            closeDrawer()
                        }
                    }
                }
        ) {
            content()
            if (offsetX.value > 0f) {
                Box(
                    Modifier.fillMaxSize()
                        .pointerInput(Unit) { detectTapGestures { closeDrawer() } }
                )
            }
        }
    }
}

