package com.example.cs446.backend.data.model
import com.squareup.moshi.Json
import kotlinx.datetime.Instant
import java.util.UUID

data class Pet(
    val id: Int,
    @Json(name = "created_at") val createdAt: Instant,
    val name: String,
    val species: Int,
    val breed: String?,
    @Json(name = "creator_id") val creatorId: UUID,
    val birthdate: Instant,
    val weight: Double
)
