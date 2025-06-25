package com.example.cs446.backend.data.repository

import com.example.cs446.backend.SupabaseClient
import com.example.cs446.backend.data.model.User
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.auth.auth
import java.util.UUID

class UserRepository {
    private val usersTable = SupabaseClient.supabase.from("users")

    suspend fun createNewUser(userId: String, email: String): User {
        usersTable.insert(
            mapOf(
                "id" to userId,
                "username" to userId,
                "email" to email,
            )
        )
        val user = usersTable.select {
            filter {
                eq("id", userId)
            }
        }.decodeSingle<User>()
        return user
    }

    suspend fun getUserById(userId: UUID): User? {
        return try {
            usersTable.select {
                filter {
                    eq("id", userId)
                }
            }.decodeSingle<User>()
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    fun getCurrentUserId(): UUID? {
        val currentUser = SupabaseClient.supabase.auth.currentUserOrNull()?.id ?: return null
        return UUID.fromString(currentUser)
    }
}
