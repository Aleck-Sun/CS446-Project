package com.example.cs446.backend.component.security

import com.example.cs446.backend.data.repository.AuthRepository
import com.example.cs446.backend.data.result.AuthResult

class SecurityComponent {
    private val repository = AuthRepository()

    suspend fun loginUser(
        email: String,
        password: String,
    ): AuthResult {
        return repository.loginWithSupabase(email, password)
    }

    suspend fun registerUser(
        email: String,
        password: String
    ): AuthResult {
        return repository.registerWithSupabase(email, password)
    }

    suspend fun logoutUser(): Boolean {
        return repository.logout()
    }
}