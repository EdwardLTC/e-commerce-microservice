package org.edward.app.presentations.screens.main.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.koin.koinNavigatorScreenModel
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.core.component.KoinComponent

class ProfileScreen : Tab, KoinComponent {

    override val options: TabOptions
        @Composable
        get() = TabOptions(
            index = 1u,
            icon = rememberVectorPainter(Icons.Default.Person),
            title = "Profile",
        )

    @OptIn(ExperimentalMaterial3Api::class)
    @Preview
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = navigator.koinNavigatorScreenModel<ProfileScreenModel>()

        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            CenterAlignedTopAppBar(title = {
                Text(
                    "Account",
                    fontWeight = FontWeight.W600,
                    fontSize = TextUnit(18.0.toFloat(), TextUnitType.Sp),
                )
            })
            UserInformation()
            GeneralInformation {
                screenModel.clearDataStore {

                }
            }
        }
    }

    @Preview
    @Composable
    fun UserInformation() {

        val url =
            "https://plus.unsplash.com/premium_photo-1749669869018-8a33825100f0?q=80&w=988&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D"

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (url.isNotBlank()) {
                Box(
                    Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                ) {
                    KamelImage(
                        { asyncPainterResource(url) },
                        contentDescription = url,
                        modifier = Modifier.size(80.dp),
                        alignment = Alignment.Center,
                        contentScale = ContentScale.Crop,
                        alpha = DefaultAlpha,
                        contentAlignment = Alignment.Center,
                    )
                }
            } else {
                Box(
                    Modifier.size(80.dp)
                        .background(MaterialTheme.colorScheme.primary, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person, contentDescription = "Default icon",
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.surface
                    )
                }

            }

            Spacer(Modifier.width(12.dp))

            Column(
                verticalArrangement = Arrangement.Center,
            ) {
                Text("Operator", style = MaterialTheme.typography.bodyMedium)
                Text(
                    "operator@gmail.com",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }

    @Preview
    @Composable
    fun GeneralInformation(onLogout: () -> Unit) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
                .padding(horizontal = 16.dp)
        ) {
            Text("General Information", style = MaterialTheme.typography.titleMedium)
            General()
            Text(
                "Delete Account",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.W600
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "Logout",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.W600,
                modifier = Modifier.clickable { onLogout() }
            )

        }
    }

    @Preview
    @Composable
    fun General() {
        val screenModel = koinScreenModel<ProfileScreenModel>()
        val navigator = LocalNavigator.currentOrThrow

        val items = listOf(
            "Account Information",
            "Forgot Password",
            "Change Language",
            "Settings"
        )

        Column(modifier = Modifier.fillMaxWidth()) {
            items.forEachIndexed { index, title ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { screenModel.handleNavigate(index, navigator) }
                        .padding(vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = Color.Gray
                    )
                }

                // Divider except for the last item
                if (index != items.lastIndex) {
                    HorizontalDivider(Modifier, 1.dp, Color(0xFFE0E0E0))
                }
            }
        }
    }
}

