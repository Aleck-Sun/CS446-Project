package com.example.cs446.backend.data.model
import com.squareup.moshi.Json
import kotlinx.datetime.Instant
import java.util.UUID

data class Pet(
    val id: UUID,
    @Json(name = "created_at") val createdAt: Instant,
    val name: String,
    val species: Int, // TODO: This should be an enum, once we actually start working on pet logic
    val breed: String?,
    @Json(name = "created_by") val createdBy: UUID,
    val birthdate: Instant,
    val weight: Double,
    @Json(name = "image_url") val imageUrl: String? = null
)
