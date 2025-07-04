package com.example.cs446.backend.data.model.post

import com.squareup.moshi.Json
import java.util.UUID

data class Follow(
    @Json(name = "user_id") val userId: UUID,
    @Json(name = "pet_id") val petId: UUID,
    val followed: Boolean
)