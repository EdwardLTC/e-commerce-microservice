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

class LoginScreenModel(
    private val dataStoreRepository: DataStoreRepository,
    private val authRepository: AuthRepository,
) : ScreenModel {
    data class LoginUiState(
        val email: String = "",
        val password: String = "",
        val isLoading: Boolean = false,
        val error: String? = null
    )

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    fun onEmailChange(value: String) {
        _uiState.value = _uiState.value.copy(email = value, error = null)
    }

    fun onPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(password = value, error = null)
    }

    fun login(onSuccess: () -> Unit) {
        screenModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val state = _uiState.value
            if (state.email.isBlank() || state.password.isBlank()) {
                _uiState.value = state.copy(isLoading = false, error = "Email and password cannot be empty")
                return@launch
            }

            when (val result = authRepository.login(LoginRequest(email = state.email, password = state.password))) {
                is AsyncResult.Success -> {
                    dataStoreRepository.saveAccessToken(result.data.accessToken, 3600 * 24 * 7)
                    dataStoreRepository.saveRefreshToken(result.data.accessToken, 3600 * 24 * 30)
                    dataStoreRepository.saveUserEmail(state.email)
                    dataStoreRepository.saveUserName(
                        state.email.substringBefore("@").replaceFirstChar { it.uppercase() }
                    )
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    onSuccess()
                }

                is AsyncResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.displayMessage ?: "Login failed"
                    )
                }
            }
        }
    }
}
