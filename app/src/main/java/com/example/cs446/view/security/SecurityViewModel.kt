package com.example.cs446.view.security

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cs446.common.SecurityComponent
import com.example.cs446.backend.data.result.AuthResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


class SecurityViewModel : ViewModel() {
    private val authorizer = SecurityComponent()

    private val _authState = MutableStateFlow<AuthResult>(AuthResult.Loading)
    val authState: StateFlow<AuthResult> = _authState

    fun signUp(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthResult.Loading
            _authState.value = authorizer.registerUser(email, password)
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthResult.Loading
            _authState.value = authorizer.loginUser(email, password)
        }
    }
}