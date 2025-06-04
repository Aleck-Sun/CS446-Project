package com.example.cs446.data.model
import com.squareup.moshi.Json
import java.time.OffsetDateTime
import java.time.LocalDate

data class Pet(
    val id: Int,
    @Json(name = "created_at") val createdAt: String,
    val name: String,
    val species: Int,
    val breed: String?,
    @Json(name = "creator_id") val creatorId: Int,
    val birthdate: String,
    val weight: Double
)
