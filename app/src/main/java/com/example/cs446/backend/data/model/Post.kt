package com.example.cs446.backend.data.model

import android.location.Location
import com.squareup.moshi.Json
import kotlinx.datetime.Instant
import java.util.UUID

data class Post(
    val id: UUID,
    @Json(name = "user_id") val userId: UUID,
    @Json(name = "pet_id") val petId: UUID,
    @Json(name = "created_at") val createdAt: Instant,
    val text: String,
    @Json(name = "photo_urls") val photoUrls: List<String>,
    val userProfileUrl: String? = null,
    val comments: List<Comment> = emptyList(),
    val location: Location? = null,
    val authorName: String? = null,
    val petName: String? = null,
    val likes: Int = 0,
    val liked: Boolean = false,
    val isFollowing: Boolean = false
)