package org.edward.app.presentations.navigations

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.tab.CurrentTab
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import org.edward.app.presentations.screens.main.home.HomeScreen
import org.edward.app.presentations.screens.main.profile.ProfileScreen

class BottomNav(private val firstScreen: Tab = HomeScreen()) : Screen {
    @Composable
    override fun Content() {
        TabNavigator(firstScreen) {
            Surface(modifier = Modifier.fillMaxSize()) {
                Scaffold(
                    bottomBar = {
                        NavigationBar(
                            modifier = Modifier
                                .padding(horizontal = 8.dp, vertical = 8.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .shadow(4.dp, RoundedCornerShape(20.dp)),
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 3.dp
                        )
                        {
                            AnimatedTabNavigationItem(firstScreen)
                            AnimatedTabNavigationItem(ProfileScreen())
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        CurrentTab()
                    }
                }
            }
        }
    }
}

@Composable
private fun RowScope.AnimatedTabNavigationItem(tab: Tab) {
    val tabNavigator = LocalTabNavigator.current
    val selected = tabNavigator.current == tab

    val iconSize by animateDpAsState(
        targetValue = if (selected) 28.dp else 24.dp,
        animationSpec = tween(durationMillis = 200),
        label = "iconSizeAnim"
    )

    val iconTint by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(durationMillis = 200),
        label = "iconColorAnim"
    )

    NavigationBarItem(
        selected = selected,
        onClick = { tabNavigator.current = tab },
        icon = {
            tab.options.icon?.let {
                Icon(
                    painter = it,
                    contentDescription = tab.options.title,
                    modifier = Modifier.size(iconSize),
                    tint = iconTint
                )
            }
        },
        label = {
            AnimatedContent(
                targetState = selected,
                transitionSpec = {
                    fadeIn(tween(150)) togetherWith fadeOut(tween(150))
                },
                label = "labelAnim"
            ) { show ->
                if (show) {
                    Text(
                        text = tab.options.title,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 12.sp
                    )
                }
            }
        },
        alwaysShowLabel = false
    )
}
