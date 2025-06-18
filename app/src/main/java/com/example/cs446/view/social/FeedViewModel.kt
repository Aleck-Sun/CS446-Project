package com.example.cs446.view.social

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cs446.backend.data.model.Comment
import com.example.cs446.backend.data.model.Pet
import com.example.cs446.backend.data.model.Post
import com.example.cs446.backend.data.repository.PetRepository
import com.example.cs446.backend.data.repository.PostRepository
import com.example.cs446.backend.data.result.PostResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import java.util.UUID

class FeedViewModel : ViewModel() {
    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts

    private val _pets = MutableStateFlow<List<Pet>>(emptyList())
    val pets: StateFlow<List<Pet>> = _pets

    private val postRepository = PostRepository()
    private val petRepository = PetRepository()

    private val _postState = MutableStateFlow<PostResult>(PostResult.Idling)
    val postState: StateFlow<PostResult> = _postState

    private var isLoading = false
    private var currentPage = 0

    init {
        loadMorePosts()
        getPetsWithPostPermissions()
    }

    fun getPetsWithPostPermissions() {
        viewModelScope.launch {
            _pets.value = petRepository.getPetsByPostPermission()
        }
    }

    fun uploadPost(
        context: Context,
        petId: UUID,
        caption: String,
        imageUris: List<Uri>
    ) {
        viewModelScope.launch {
            _postState.value = PostResult.Posting
            try {
                val imageUrls = postRepository.uploadPostImages(
                    context,
                    imageUris,
                    petId
                )
                postRepository.uploadPost(
                    imageUrls,
                    petId,
                    caption
                )
                _postState.value = PostResult.PostSuccess
            } catch (_: Exception) {
                _postState.value = PostResult.PostError("Failed to post.")
            }
        }
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
                photoUrls = listOf(postRepository.getSignedImageUrl(
                        "aed3caa8-e122-435e-8a44-f6965b9db847/1750284634404.jpg"
                    ),
                    postRepository.getSignedImageUrl(
                        "aed3caa8-e122-435e-8a44-f6965b9db847/1750285564480.jpg"
                    )),
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
