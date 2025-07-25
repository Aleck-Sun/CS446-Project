package com.example.cs446.backend.data.model.post

import com.squareup.moshi.Json
import java.util.UUID

data class Like(
    @Json(name = "user_id") val userId: UUID,
    @Json(name = "post_id") val postId: UUID,
    val liked: Boolean
)