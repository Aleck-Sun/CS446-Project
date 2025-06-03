package com.example.cs446.data.repository

import com.example.cs446.SupabaseClient
import com.example.cs446.data.model.Pet
import io.github.jan.supabase.postgrest.from

class UserRepository {
    private val usersTable = SupabaseClient.supabase.from("users")

    suspend fun createNewUser(userId: Int) {

    }
}