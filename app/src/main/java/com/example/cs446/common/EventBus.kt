package com.example.cs446.common

import com.example.cs446.backend.data.model.BadgeTier
import com.example.cs446.backend.data.model.BadgeType
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.util.UUID

sealed class AppEvent {
    data class PostCreated(val petId: UUID, val postId: UUID) : AppEvent()
    data class ImageUploaded(val petId: UUID) : AppEvent()
    data class CommentPosted(val postId: UUID) : AppEvent()
    data class ActivityLogged(val petId: UUID, val logId: UUID) : AppEvent()
    data class BadgeEarned(
        val petId: UUID,
        val badgeType: BadgeType,
        val badgeTier: BadgeTier
    ) : AppEvent()
    object LoggedIn : AppEvent()
}

object EventBus {
    private val _events = MutableSharedFlow<AppEvent>()
    val events = _events.asSharedFlow()

    suspend fun emit(event: AppEvent) {
        _events.emit(event)
    }

    fun tryEmit(event: AppEvent) {
        _events.tryEmit(event)
    }
}
