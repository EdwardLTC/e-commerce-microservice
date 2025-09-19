package org.edward.app.presentations.navigations

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.tab.CurrentTab
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.edward.app.presentations.screens.main.home.HomeScreen
import org.edward.app.presentations.screens.main.profile.ProfileScreen

class DrawerNav(private val firstScreen: Tab = HomeScreen()) : Screen {
    @Composable
    override fun Content() {
        TabNavigator(firstScreen) {
            val drawerState = rememberDrawerState(DrawerValue.Closed)
            val scope = rememberCoroutineScope()
            val tabNavigator = LocalTabNavigator.current

            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    ModalDrawerSheet {
                        DrawerTabItem(firstScreen, tabNavigator, drawerState, scope)
                        DrawerTabItem(ProfileScreen(), tabNavigator, drawerState, scope)
                    }
                }
            ) {
                Scaffold { innerPadding ->
                    CurrentTab()
                }
            }
        }
    }
}

@Composable
fun DrawerTabItem(
    tab: Tab,
    tabNavigator: TabNavigator,
    drawerState: DrawerState,
    scope: CoroutineScope
) {
    val selected = tabNavigator.current == tab

    NavigationDrawerItem(
        label = { Text(tab.options.title) },
        selected = selected,
        onClick = {
            tabNavigator.current = tab
            scope.launch { drawerState.close() }
        },
        icon = {
            tab.options.icon?.let {
                Icon(painter = it, contentDescription = tab.options.title)
            }
        },
        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
    )
}
