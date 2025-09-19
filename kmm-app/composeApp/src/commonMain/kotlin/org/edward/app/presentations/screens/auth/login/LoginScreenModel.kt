package org.edward.app.presentations.screens.auth.login

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.edward.app.data.local.DataStoreRepository
import org.edward.app.data.remote.auth.AuthRepository
import org.edward.app.data.remote.auth.LoginRequest
import org.edward.app.data.utils.AsyncResult
import org.koin.core.component.KoinComponent

class LoginScreenModel(
    private val dataStoreRepository: DataStoreRepository,
    private val authRepository: AuthRepository,
) : ScreenModel, KoinComponent {
    data class LoginUiState(
        val email: String = "mor_2314",
        val password: String = "83r5^_",
        val isLoading: Boolean = false,
        val error: String? = null
    )

    private val _uiState = MutableStateFlow(LoginUiState())

    val uiState: StateFlow<LoginUiState> = _uiState

    fun onEmailChange(newEmail: String) {
        _uiState.value = _uiState.value.copy(email = newEmail)
    }

    fun onPasswordChange(newPassword: String) {
        _uiState.value = _uiState.value.copy(password = newPassword)
    }

    fun login(onSuccess: () -> Unit) {
        screenModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            if (_uiState.value.email.isBlank() || _uiState.value.password.isBlank()) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Email and password cannot be empty"
                )
                return@launch
            }

            when (val result =
                authRepository.login(
                    LoginRequest(
                        username = _uiState.value.email,
                        password = _uiState.value.password
                    )
                )) {
                is AsyncResult.Success -> {
                    dataStoreRepository.saveAccessToken(result.data.token, 3600)
                    dataStoreRepository.saveRefreshToken(result.data.token, 86400)
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    onSuccess()
                }

                is AsyncResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.displayMessage
                    )
                }
            }
        }
    }
}