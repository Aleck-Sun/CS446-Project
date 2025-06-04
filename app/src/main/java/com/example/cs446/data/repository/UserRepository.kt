package com.example.cs446.data.repository

import com.example.cs446.SupabaseClient
import com.example.cs446.data.model.User
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
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
        println("User inserted.")
        return user
    }
}