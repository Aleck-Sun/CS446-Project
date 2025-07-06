package com.example.cs446.common

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.util.UUID

sealed class AppEvent {
    data class PostCreated(val postId: UUID) : AppEvent()
    data class ImageUploaded(val petId: UUID) : AppEvent()
    data class CommentPosted(val postId: UUID) : AppEvent()
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
