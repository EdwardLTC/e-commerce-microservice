package org.edward.app.presentations.screens.main.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.AddBox
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.koin.koinNavigatorScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import org.edward.app.presentations.navigations.RootAppDestination
import org.edward.app.presentations.navigations.replaceAll
import org.edward.app.presentations.screens.main.product.create.CreateProductScreen
import org.edward.app.presentations.screens.main.profile.settings.SettingsScreen

class ProfileScreen : Tab {

    override val options: TabOptions
        @Composable
        get() = TabOptions(
            index = 2u,
            icon = rememberVectorPainter(Icons.Default.Person),
            title = "Profile",
        )

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = navigator.koinNavigatorScreenModel<ProfileScreenModel>()
        val profileState by screenModel.uiState.collectAsState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(24.dp))

            // Page title
            Text(
                "MY PAGE",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = MaterialTheme.typography.labelSmall.letterSpacing * 2,
                modifier = Modifier.padding(horizontal = 24.dp),
            )

            Spacer(Modifier.height(24.dp))

            // Avatar & user info
            ProfileHeader(
                name = profileState.userName.ifEmpty { "User" },
                email = profileState.userEmail.ifEmpty { "Not signed in" },
            )

            Spacer(Modifier.height(28.dp))

            // Order quick access
            OrderStatusRow()

            Spacer(Modifier.height(24.dp))

            // Menu sections
            MenuSection(
                items = listOf(
                    MenuItem(Icons.Outlined.Inventory2, "My Orders"),
                    MenuItem(Icons.Outlined.FavoriteBorder, "Wishlist"),
                    MenuItem(Icons.Outlined.LocationOn, "Addresses"),
                    MenuItem(Icons.Outlined.CreditCard, "Payment Methods"),
                    MenuItem(Icons.Outlined.AddBox, "Create Product") {
                        navigator.parent?.push(CreateProductScreen())
                    },
                )
            )

            Spacer(Modifier.height(16.dp))

            MenuSection(
                items = listOf(
                    MenuItem(Icons.Outlined.Notifications, "Notifications"),
                    MenuItem(Icons.Outlined.Settings, "Settings") {
                        navigator.parent?.push(SettingsScreen())
                    },
                    MenuItem(Icons.AutoMirrored.Outlined.HelpOutline, "Help & FAQ"),
                    MenuItem(Icons.Outlined.Star, "Rate the App"),
                )
            )

            Spacer(Modifier.height(16.dp))

            // Logout
            LogoutRow {
                screenModel.logout {
                    val rootNav = navigator.parent?.parent
                    rootNav?.replaceAll(RootAppDestination.Login)
                }
            }

            Spacer(Modifier.height(40.dp))
        }
    }

    @Composable
    private fun ProfileHeader(name: String, email: String) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Avatar with initials
            val initials = name
                .split(" ")
                .take(2)
                .mapNotNull { it.firstOrNull()?.uppercaseChar() }
                .joinToString("")
                .ifEmpty { "U" }

            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurface),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    initials,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.surface,
                )
            }

            Spacer(Modifier.width(16.dp))

            Column {
                Text(
                    name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    email,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(6.dp))
                // Member badge
                Box(
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.extraSmall)
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                        .padding(horizontal = 10.dp, vertical = 3.dp),
                ) {
                    Text(
                        "MEMBER",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = MaterialTheme.typography.labelSmall.letterSpacing * 1.5,
                    )
                }
            }
        }
    }

    @Composable
    private fun OrderStatusRow() {
        val statuses = listOf(
            "Processing" to Icons.Outlined.Inventory2,
            "Shipped" to Icons.Outlined.LocalShipping,
            "Delivered" to Icons.Outlined.Star,
        )

        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            Text(
                "MY ORDERS",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = MaterialTheme.typography.labelSmall.letterSpacing * 2,
            )
            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                statuses.forEach { (label, icon) ->
                    Column(
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.small)
                            .clickable { }
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Icon(
                            icon,
                            contentDescription = label,
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            label,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }

    private data class MenuItem(
        val icon: ImageVector,
        val label: String,
        val onClick: (() -> Unit)? = null,
    )

    @Composable
    private fun MenuSection(items: List<MenuItem>) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .clip(MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
        ) {
            items.forEachIndexed { index, item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { item.onClick?.invoke() }
                        .padding(horizontal = 16.dp, vertical = 15.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        item.icon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.width(14.dp))
                    Text(
                        item.label,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f),
                    )
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    )
                }
                if (index < items.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 50.dp, end = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                    )
                }
            }
        }
    }

    @Composable
    private fun LogoutRow(onLogout: () -> Unit) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .clip(MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                .clickable { onLogout() }
                .padding(horizontal = 16.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.AutoMirrored.Outlined.Logout,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.error,
            )
            Spacer(Modifier.width(14.dp))
            Text(
                "Log Out",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}
