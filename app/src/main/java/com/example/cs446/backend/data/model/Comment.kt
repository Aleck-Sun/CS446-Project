package com.example.cs446.backend.data.model

import com.squareup.moshi.Json
import kotlinx.datetime.Instant
import java.util.UUID

data class Comment(
    val id: UUID,
    @Json(name = "author_id") val authorId: UUID,
    @Json(name = "post_id") val postId: UUID,
    @Json(name = "created_at") val createdAt: Instant,
    val text: String,
    val authorName: String? = null
)
