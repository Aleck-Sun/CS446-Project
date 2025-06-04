package com.example.cs446.data.model
import com.squareup.moshi.Json
import kotlinx.datetime.Instant
import java.time.OffsetDateTime
import java.time.LocalDate
import java.util.UUID
import kotlin.uuid.Uuid

data class User(
    val id: UUID,
    @Json(name = "created_at") val createdAt: Instant,
    val username: String,
    val email: String,
    @Json(name = "avatar_url") val avatarUrl: String? = null
)
