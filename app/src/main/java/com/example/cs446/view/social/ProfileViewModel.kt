package com.example.cs446.view.social

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cs446.backend.data.model.post.Post
import com.example.cs446.backend.data.repository.PostRepository
import com.example.cs446.backend.data.repository.UserRepository
import com.example.cs446.common.AppEvent
import com.example.cs446.common.EventBus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class ProfileViewModel : ViewModel() {
    private val _avatar = MutableStateFlow<Uri?>(null)
    private val _username = MutableStateFlow<String>("TEMP_USERNAME")
    private val _bio = MutableStateFlow<String>("TEMP_BIO")

    val avatar: StateFlow<Uri?> = _avatar
    val username: StateFlow<String> = _username
    val bio: StateFlow<String> = _bio

    private val _allPosts = MutableStateFlow<List<Post>>(listOf())

    val posts: StateFlow<List<Post>> = _allPosts

    var isLoading = false;

    private val postRepository = PostRepository()
    private val userRepository = UserRepository()

    init {
        loadMorePosts()
    }

    fun updateProfile(
        avatar: Uri?,
        username: String,
        bio: String
    ) {
        _avatar.value = avatar
        _username.value = username
        _bio.value = bio
    }

    suspend fun loadNewPost(postId: UUID) {
        try {
            postRepository.loadPost(
                postId
            )?.let {
                _allPosts.value = (_allPosts.value + listOf(it)).sortedByDescending {
                    it.createdAt
                }.toMutableList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }

    fun loadMorePosts() {
        if (isLoading) return
        isLoading = true

        viewModelScope.launch {
            try {
                val newPosts = postRepository.loadProfilePosts(
                    userID = userRepository.getCurrentUserId()
                )
                _allPosts.value = (_allPosts.value + newPosts).sortedByDescending {
                    it.createdAt
                }.toMutableList()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }

            EventBus.events.collect { event ->
                when (event) {
                    is AppEvent.PostCreated -> loadNewPost(event.postId)
                    else -> null
                }
            }
        }
    }
}