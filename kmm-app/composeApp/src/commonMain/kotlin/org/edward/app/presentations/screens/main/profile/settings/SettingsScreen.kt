package org.edward.app.presentations.screens.main.profile.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.edward.app.data.local.DataStoreRepository
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.core.component.KoinComponent

class SettingsScreen : Screen, KoinComponent {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val screenModel = koinScreenModel<SettingsScreenModel>()
        val navigator = LocalNavigator.currentOrThrow

        Column(Modifier.fillMaxWidth()) {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Settings",
                        fontWeight = FontWeight.W600,
                        fontSize = TextUnit(18.0.toFloat(), TextUnitType.Sp),
                    )
                },
                navigationIcon = {
                    IconButton(
                        modifier = Modifier.size(40.dp).padding(start = 16.dp),
                        onClick = {
                            navigator.pop()
                        }) {
                        Icon(
                            imageVector = Icons.Default.ChevronLeft,
                            contentDescription = "",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
            )
            Theme(screenModel)
            Nav(screenModel)
        }

    }

    @Preview
    @Composable
    fun Theme(screenModel: SettingsScreenModel) {
        val isDarkTheme by screenModel.isDarkTheme.collectAsState()

        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Change theme",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = isDarkTheme,
                onCheckedChange = { _ -> screenModel::toggleTheme },
                enabled = true,
            )
        }
    }

    @Preview
    @Composable
    fun Nav(screenModel: SettingsScreenModel) {
        var expanded by remember { mutableStateOf(false) }
        val navType by screenModel.navType.collectAsState()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Navigation type",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )

            Box {
                TextButton(onClick = { expanded = true }) {
                    Text(navType.name)
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DataStoreRepository.Companion.NavType.entries.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type.name) },
                            onClick = {
                                expanded = false
                                screenModel.changeNavType(type)
                            }
                        )
                    }
                }
            }
        }
    }
}

