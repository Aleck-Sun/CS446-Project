package com.example.cs446.view.social

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cs446.backend.data.model.Pet
import com.example.cs446.backend.data.model.post.Post
import com.example.cs446.backend.data.repository.PetRepository
import com.example.cs446.backend.data.repository.PostRepository
import com.example.cs446.backend.data.result.PostResult
import com.example.cs446.common.AppEvent
import com.example.cs446.common.EventBus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import java.io.File
import java.net.URL
import java.util.UUID

open class FeedViewModel : ViewModel() {
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

    private val _earliestLoadedTime = MutableStateFlow<Instant?>(null)
    private val _latestLoadedTime = MutableStateFlow<Instant?>(null)

    private val _sharedText = MutableStateFlow<String?>(null)
    val sharedText: StateFlow<String?> = _sharedText

    private val _sharedImageUri = MutableStateFlow<Uri?>(null)
    val sharedImageUri: StateFlow<Uri?> = _sharedImageUri

    private val _shareContent = MutableStateFlow<Boolean>(false)
    val shareContent: StateFlow<Boolean> = _shareContent

    init {
        filterPosts()
        loadMorePosts()
        getPetsWithPostPermissions()
    }

    fun setSharedData(text: String?, imageUri: Uri?) {
        _shareContent.value = true
        _sharedText.value = text
        _sharedImageUri.value = imageUri
    }

    fun clearSharedData() {
        _shareContent.value = false
        _sharedText.value = null
        _sharedImageUri.value = null
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
                val postId = postRepository.uploadPost(
                    imageUrls,
                    petId,
                    caption,
                    isPublic
                )
                _postState.value = PostResult.PostSuccess
                EventBus.emit(
                    AppEvent.PostCreated(
                        petId,
                        postId
                    )
                )
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

    fun updateFollowStatus(petId: UUID, isFollowing: Boolean) {
        viewModelScope.launch {
            try {
                if (postRepository.updateFollowStatus(petId, isFollowing)) {
                    _allPosts.value = _allPosts.value.map {
                            post ->
                        if (post.petId == petId) {
                            post.copy(isFollowing = !isFollowing)
                        } else {
                            post
                        }
                    }
                }

                filterPosts()
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }
}
