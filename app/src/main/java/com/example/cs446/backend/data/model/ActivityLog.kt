package com.example.cs446.backend.data.model
import com.squareup.moshi.Json
import java.time.Instant
import java.util.UUID

data class ActivityLog(
    @Json(name = "user_id") val userId: UUID,
    @Json(name = "pet_id") val petId: UUID,
    @Json(name = "created_at") val createdAt: Instant,
    val activityType: String,
    val comment: String,
)