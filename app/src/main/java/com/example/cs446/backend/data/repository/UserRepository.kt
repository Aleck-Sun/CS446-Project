package com.example.cs446.backend.data.repository

import com.example.cs446.backend.SupabaseClient
import com.example.cs446.backend.data.model.User
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.auth.auth
import java.util.UUID
import kotlin.time.Duration

class UserRepository {
    private val usersTable = SupabaseClient.supabase.from("users")
    val storage = SupabaseClient.supabase.storage

    val defaultAvatarUrl = "user.png"
    // TODO: Add Bio attribute to users table
    val defaultBio = "Add Biography"

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

    suspend fun getSignedAvatarUrl(avatarUrl: String): String {
        return try {
            storage.from("avatars").createSignedUrl(
                path = avatarUrl,
                expiresIn = Duration.parse("12h")  // expires in 12 hours
            )
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    suspend fun getBioByUser(user: User): String {
        return defaultBio
    }

    suspend fun getUserById(userId: UUID): User? {
        return try {
            val user = usersTable.select {
                filter {
                    eq("id", userId)
                }
            }.decodeSingle<User>()

            return user.copy(
                avatarUrl = getSignedAvatarUrl(user.avatarUrl?:defaultAvatarUrl)
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    suspend fun getUsersByIds(userIds: List<UUID>): List<User> {
        return try {
            usersTable.select {
                filter {
                    isIn("id", userIds)
                }
            }.decodeList<User>().map {
                it.copy(
                    avatarUrl = getSignedAvatarUrl(it.avatarUrl ?: defaultAvatarUrl)
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return emptyList()
        }
    }

    fun getCurrentUserId(): UUID? {
        val currentUserId = SupabaseClient.supabase.auth.currentUserOrNull()?.id
        return currentUserId?.let { UUID.fromString(it) }
    }
}
