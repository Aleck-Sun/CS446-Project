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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import java.util.UUID

class FeedViewModel : ViewModel() {
    private val _posts = MutableStateFlow<List<Post>>(listOf())
    val posts: StateFlow<List<Post>> = _posts

    private val _pets = MutableStateFlow<List<Pet>>(emptyList())
    val pets: StateFlow<List<Pet>> = _pets

    // TODO: Move these to backend component
    private val postRepository = PostRepository()
    private val petRepository = PetRepository()

    private val _postState = MutableStateFlow<PostResult>(PostResult.Idling)
    val postState: StateFlow<PostResult> = _postState

    private var isLoading = false
    private var currentPage = 0

    private val _earliestLoadedTime = MutableStateFlow<Instant?>(null)
    private val _latestLoadedTime = MutableStateFlow<Instant?>(null)

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
        imageUris: List<Uri>,
        isPublic: Boolean = false,
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
                    caption,
                    isPublic
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
            val newPosts = postRepository.loadPosts(
                createdAfter = _latestLoadedTime.value,
                createdBefore = _earliestLoadedTime.value
            )
            _posts.value = (_posts.value + newPosts).sortedByDescending {
                it.createdAt
            }.toMutableList()
            _earliestLoadedTime.value = _posts.value.lastOrNull()?.createdAt
            _latestLoadedTime.value = _posts.value.firstOrNull()?.createdAt

            currentPage++
            isLoading = false
        }
    }

    fun uploadComment(postId: UUID, text: String) {
        viewModelScope.launch {
            val comment = postRepository.uploadComment(postId, text)
            comment?.let {
                _posts.value = _posts.value.map {
                    post ->
                    if (post.id == postId) {
                        val updatedComments = post.comments.toMutableList()
                        updatedComments.add(it)
                        post.copy(comments = updatedComments)
                    } else {
                        post
                    }
                }
            }
        }
    }

    fun likePost(postId: UUID) {
        viewModelScope.launch {
            val liked = postRepository.updateLikeStatus(postId)
            _posts.value = _posts.value.map {
                if (it.id == postId) {
                    it.copy(
                        liked = liked,
                        likes = postRepository.getLikesForPost(it.id)
                    )
                } else {
                    it
                }
            }
        }
    }
}
