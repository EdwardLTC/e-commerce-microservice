package org.edward.app.presentations.navigations

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.tab.CurrentTab
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import org.edward.app.presentations.screens.main.cart.CartScreen
import org.edward.app.presentations.screens.main.home.HomeScreen
import org.edward.app.presentations.screens.main.profile.ProfileScreen

private data class NavItem(
    val tab: Tab,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

class BottomNav(private val firstScreen: Tab = HomeScreen()) : Screen {
    @Composable
    override fun Content() {
        TabNavigator(firstScreen) {
            val tabNavigator = LocalTabNavigator.current
            val isHomeFeed = tabNavigator.current.options.index == 0.toUShort()

            val items = remember {
                listOf(
                    NavItem(HomeScreen(), "HOME", Icons.Filled.Home, Icons.Outlined.Home),
                    NavItem(CartScreen(), "CART", Icons.Filled.ShoppingCart, Icons.Outlined.ShoppingCart),
                    NavItem(ProfileScreen(), "MY PAGE", Icons.Filled.Person, Icons.Outlined.Person),
                )
            }

            Box(modifier = Modifier.fillMaxSize()) {
                // Tab content fills the entire area
                Box(
                    modifier = if (isHomeFeed) {
                        Modifier.fillMaxSize()
                    } else {
                        Modifier
                            .fillMaxSize()
                            .padding(bottom = 56.dp)
                    }
                ) {
                    CurrentTab()
                }

                // Bottom nav bar pinned at the bottom
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(
                            if (isHomeFeed)
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
                            else
                                MaterialTheme.colorScheme.surface
                        )
                        .windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        items.forEach { item ->
                            BottomNavTab(item, tabNavigator)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RowScope.BottomNavTab(item: NavItem, tabNavigator: TabNavigator) {
    val selected = tabNavigator.current.options.index == item.tab.options.index
    val tint = if (selected)
        MaterialTheme.colorScheme.onSurface
    else
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)

    Column(
        modifier = Modifier
            .weight(1f)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) { tabNavigator.current = item.tab }
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
            contentDescription = item.label,
            modifier = Modifier.size(22.dp),
            tint = tint,
        )
        Text(
            text = item.label,
            style = MaterialTheme.typography.labelSmall,
            color = tint,
            modifier = Modifier.padding(top = 2.dp),
            letterSpacing = MaterialTheme.typography.labelSmall.letterSpacing * 1.2,
        )
    }
}
