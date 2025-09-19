package org.edward.app.presentations.screens.auth.login

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.launch
import multiplatform_app.composeapp.generated.resources.Res
import multiplatform_app.composeapp.generated.resources.facebook
import multiplatform_app.composeapp.generated.resources.google
import multiplatform_app.composeapp.generated.resources.ic_cyclone
import org.edward.app.presentations.navigations.RootAppDestination
import org.edward.app.presentations.navigations.replaceAll
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.core.component.KoinComponent

class LoginScreen : Screen, KoinComponent {

    @Composable
    @Preview
    override fun Content() {
        val screenModel = koinScreenModel<LoginScreenModel>()

        val uiState by screenModel.uiState.collectAsState()

        var passwordVisible by remember { mutableStateOf(false) }

        val navigator = LocalNavigator.currentOrThrow

        val scope = rememberCoroutineScope()

        val alpha = remember { Animatable(0f) }
        val scale = remember { Animatable(0.8f) }
        val rotation = remember { Animatable(20f) }

        val alphaText = remember { Animatable(0f) }
        val offsetY = remember { Animatable(20f) }

        LaunchedEffect(Unit) {
            scope.launch {
                alpha.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 400)
                )
            }
            scope.launch {
                scale.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 400)
                )
            }
            scope.launch {
                rotation.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = 400)
                )
            }

            scope.launch {
                alphaText.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(600)
                )
            }
            scope.launch {
                offsetY.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(700)
                )
            }
        }

        Column(modifier = Modifier.fillMaxSize().padding(top = 40.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Image(
                    painter = painterResource(Res.drawable.ic_cyclone),
                    contentDescription = "",
                    modifier = Modifier.size(100.dp)
                        .scale(scale.value)
                        .alpha(alpha.value)
                        .rotate(rotation.value),
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                    .background(MaterialTheme.colorScheme.background)
                    .padding(24.dp)
                    .alpha(alphaText.value)
                    .offset(y = offsetY.value.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text("Welcome Back", fontSize = 22.sp, fontWeight = FontWeight.Bold)

                Text("Enter your details below", fontSize = 14.sp, color = Color.Gray)

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = uiState.email,
                    onValueChange = screenModel::onEmailChange,
                    placeholder = { Text("Enter your email") },
                    label = { Text("Email Address") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = uiState.password,
                    onValueChange = screenModel::onPasswordChange,
                    placeholder = { Text("Enter your password") },
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    leadingIcon = { Icon(Icons.Default.Key, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = null
                            )
                        }
                    },
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    "Forgot your password?",
                    fontSize = 14.sp,
                    modifier = Modifier.fillMaxWidth().clickable { },
                    textAlign = TextAlign.Right
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        screenModel::login {
                            navigator.replaceAll(RootAppDestination.MainNav)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    contentPadding = PaddingValues(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Sign in",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Don’t have an account?")
                    Text(
                        "Register",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { }
                            .padding(horizontal = 4.dp, vertical = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    HorizontalDivider(
                        Modifier.weight(1f),
                        DividerDefaults.Thickness,
                        Color.LightGray
                    )
                    Text("  Or sign in with  ", fontSize = 14.sp, color = Color.Gray)
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        thickness = DividerDefaults.Thickness,
                        color = Color.LightGray
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SocialButton("Login With Google", Res.drawable.google)
                    SocialButton("Login With Facebook", Res.drawable.facebook)
                }
            }
        }
    }

    @Composable
    fun SocialButton(platform: String, resource: DrawableResource) {
        OutlinedButton(
            onClick = { },
            modifier = Modifier.height(48.dp).fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Image(
                painter = painterResource(resource),
                modifier = Modifier.size(36.dp),
                contentDescription = "",
            )
            Spacer(Modifier.width(8.dp))
            Text(platform, fontSize = 14.sp)
        }
    }
}