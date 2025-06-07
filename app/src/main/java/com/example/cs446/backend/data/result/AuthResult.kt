package com.example.cs446.backend.data.result

sealed class AuthResult {
    data object LoginSuccess : AuthResult()
    data class LoginError(val message: String) : AuthResult()
    data object RegisterSuccess : AuthResult()
    data class RegisterError(val message: String) : AuthResult()
    data object Loading : AuthResult()
}
