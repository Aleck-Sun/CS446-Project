package com.example.cs446.backend.data.model.post

import android.location.Location
import com.example.cs446.backend.data.model.post.Comment
import com.squareup.moshi.Json
import kotlinx.datetime.Instant
import java.util.UUID

data class PostRaw(
    val id: UUID,
    @Json(name = "user_id") val userId: UUID,
    @Json(name = "pet_id") val petId: UUID,
    @Json(name = "created_at") val createdAt: Instant,
    val caption: String,
    @Json(name = "image_urls") val imageUrls: List<String>,
    val location: String? = null, // Loaded as string from database,
    @Json(name = "is_public") val isPublic: Boolean
)

data class Post(
    val id: UUID,
    val userId: UUID,
    val petId: UUID,
    val createdAt: Instant,
    val caption: String,
    val imageUrls: List<String>,
    val isPublic: Boolean = false,
    val petImageUrl: String? = null,
    val comments: List<Comment> = listOf<Comment>(),
    val location: Location? = null,
    val authorName: String? = null,
    val petName: String? = null,
    val likes: Int = 0,
    val liked: Boolean = false,
    val isFollowing: Boolean = false,
)