package com.example.cs446.data.model
import com.squareup.moshi.Json
import kotlinx.datetime.Instant
import java.util.UUID

data class Pet(
    val id: UUID,
    @Json(name = "created_at") val createdAt: Instant,
    val name: String,
    val species: Int, // TODO: This should be an enum, once we actually start working on pet logic
    val breed: String?,
    @Json(name = "creator_id") val creatorId: UUID,
    val birthdate: Instant,
    val weight: Double,
    @Json(name = "image_url") val imageUrl: String? = null
)
