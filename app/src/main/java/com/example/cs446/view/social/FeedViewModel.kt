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
    private val _allPosts = MutableStateFlow<List<Post>>(listOf())
    private val _searchQuery = MutableStateFlow("")
    private val _filteredPosts = MutableStateFlow<List<Post>>(listOf())
    
    val posts: StateFlow<List<Post>> = _filteredPosts
    val searchQuery: StateFlow<String> = _searchQuery

    private val _pets = MutableStateFlow<List<Pet>>(emptyList())
    val pets: StateFlow<List<Pet>> = _pets

    // TODO: Move these to backend component
    private val postRepository = PostRepository()
    private val petRepository = PetRepository()

    private val _postState = MutableStateFlow<PostResult>(PostResult.Idling)
    val postState: StateFlow<PostResult> = _postState

    private var isLoading = false
    private var currentPage = 0

    val _earliestLoadedTime = MutableStateFlow<Instant?>(null)
    val _latestLoadedTime = MutableStateFlow<Instant?>(null)

    init {
        filterPosts()
        loadMorePosts()
        getPetsWithPostPermissions()
    }

    private fun filterPosts() {
        val query = _searchQuery.value.lowercase().trim()
        val allPosts = _allPosts.value
        
        _filteredPosts.value = if (query.isEmpty()) {
            allPosts
        } else {
            allPosts.filter { post ->
                post.caption.lowercase().contains(query) ||
                post.petName?.lowercase()?.contains(query) == true ||
                post.authorName?.lowercase()?.contains(query) == true
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        filterPosts()
    }

    fun clearSearch() {
        _searchQuery.value = ""
        filterPosts()
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
                loadMorePosts()
            } catch (_: Exception) {
                _postState.value = PostResult.PostError("Failed to post.")
            }
        }
    }

    fun loadMorePosts() {
        if (isLoading) return
        isLoading = true

        viewModelScope.launch {
            try {
                val newPosts = postRepository.loadPosts(
                    createdAfter = _latestLoadedTime.value,
                    createdBefore = _earliestLoadedTime.value
                )
                _allPosts.value = (_allPosts.value + newPosts).sortedByDescending {
                    it.createdAt
                }.toMutableList()
                _earliestLoadedTime.value = _allPosts.value.lastOrNull()?.createdAt
                _latestLoadedTime.value = _allPosts.value.firstOrNull()?.createdAt

                filterPosts()

                currentPage++
            } catch (e: Exception) {
                e.printStackTrace()
                // show existing posts
                filterPosts()
            } finally {
                isLoading = false
            }
        }
    }

    fun uploadComment(postId: UUID, text: String) {
        viewModelScope.launch {
            try {
                val comment = postRepository.uploadComment(postId, text)
                comment?.let {
                    _allPosts.value = _allPosts.value.map {
                        post ->
                        if (post.id == postId) {
                            val updatedComments = post.comments.toMutableList()
                            updatedComments.add(it)
                            post.copy(comments = updatedComments)
                        } else {
                            post
                        }
                    }
                    // update filtered posts after adding comment
                    filterPosts()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun likePost(postId: UUID) {
        viewModelScope.launch {
            try {
                val liked = postRepository.updateLikeStatus(postId)
                _allPosts.value = _allPosts.value.map {
                    if (it.id == postId) {
                        it.copy(
                            liked = liked,
                            likes = postRepository.getLikesForPost(it.id)
                        )
                    } else {
                        it
                    }
                }
                // update filtered posts after liking
                filterPosts()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
