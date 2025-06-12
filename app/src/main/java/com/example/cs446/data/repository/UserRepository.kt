package com.example.cs446.data.repository

import com.example.cs446.backend.SupabaseClient
import com.example.cs446.data.model.User
import io.github.jan.supabase.postgrest.from

class UserRepository {
    private val usersTable = SupabaseClient.supabase.from("users")

    suspend fun createNewUser(userId: String, email: String): User {
        usersTable.insert(
            mapOf(
                "id" to userId,
                "username" to email.split("@")[0],
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
}
