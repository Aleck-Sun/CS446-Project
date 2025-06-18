package com.example.cs446.backend.data.model
import com.squareup.moshi.Json
import java.time.Instant
import java.util.UUID

data class User(
    val id: UUID,
    @Json(name = "created_at") val createdAt: Instant,
    val username: String,
    val email: String,
    @Json(name = "avatar_url") val avatarUrl: String? = null
)
