package com.example.cs446.view.social

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cs446.data.model.Comment
import com.example.cs446.data.model.Post
import com.example.cs446.data.repository.PostRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import java.util.UUID

class FeedViewModel : ViewModel() {
    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts

    private val postRepository = PostRepository()

    private var isLoading = false
    private var currentPage = 0

    init {
        loadMorePosts()
    }

    fun loadMorePosts() {
        if (isLoading) return
        isLoading = true

        viewModelScope.launch {
            val newPosts = simulateBackendFetch(currentPage)
            _posts.value += newPosts
            currentPage++
            isLoading = false
        }
    }

    private suspend fun simulateBackendFetch(page: Int): List<Post> {
        delay(1000) // simulate network delay
        val start = page * 10
        return (start until start + 10).map {
            Post(
                id = UUID.randomUUID(),
                userId = UUID.randomUUID(),
                petId = UUID.randomUUID(),
                createdAt = Instant.fromEpochSeconds(1749934670),
                photoUrls = listOf(postRepository.getSignedImageUrl("dog1.jpeg")),
                text = "Cute puppy at play!",
                location = null,
                authorName = "Jane Doe",
                petName = "Rex",
                comments = listOf(
                    Comment(
                        id = UUID.randomUUID(),
                        authorId = UUID.randomUUID(),
                        postId = UUID.randomUUID(),
                        createdAt = Instant.fromEpochSeconds(1749934670),
                        text = "So cute! <3",
                        authorName = "John Doe"
                    )

                )
            )
        }
    }
}
