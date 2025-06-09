package com.example.cs446.backend.data.repository

import com.example.cs446.backend.SupabaseClient
import com.example.cs446.backend.SupabaseClient.supabase
import com.example.cs446.backend.data.result.AuthResult
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email

class AuthRepository {
    private val userRepository = UserRepository()

    suspend fun registerWithSupabase(email: String, password: String): AuthResult {
        try {
            supabase.auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }
            val userId = supabase.auth.currentSessionOrNull()?.user?.id
            if (userId != null) {
                try {
                    userRepository.createNewUser(userId, email)
                } catch (e: Exception) {
                    return AuthResult.RegisterError("Registration failed.")
                }

                return AuthResult.RegisterSuccess
            }
        } catch (e: Exception) {
            return AuthResult.RegisterError("Registration failed.")
        }
        return AuthResult.RegisterError("Registration failed.")
    }

    suspend fun loginWithSupabase(email: String, password: String): AuthResult {
        try {
            supabase.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            return AuthResult.LoginSuccess
        } catch (e: Exception) {
            return AuthResult.LoginError("Invalid credentials.")
        }
    }
}