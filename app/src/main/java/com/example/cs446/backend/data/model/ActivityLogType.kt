package com.example.cs446.backend.data.model

import com.squareup.moshi.Json
import java.time.Instant
import java.util.UUID

data class ActivityLogType(
    val activityType: String,
    @Json(name = "pet_id") val petId: UUID,
    @Json(name = "created_at") val createdAt: Instant,
)