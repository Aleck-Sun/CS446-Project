package com.example.cs446.backend.data.result

sealed class PostResult {
    data object PostSuccess : PostResult()
    data class PostError(val message: String) : PostResult()
    data object Posting : PostResult()
    data object Idling : PostResult()
}
