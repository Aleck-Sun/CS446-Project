package com.example.cs446.backend.data.model

import kotlinx.datetime.Instant
import java.util.UUID

data class Comment(
    val id: UUID,
    val authorId: UUID,
    val postId: UUID,
    val createdAt: Instant,
    val text: String,
    val authorName: String? = null
)
