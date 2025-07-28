package com.example.cs446.view.social

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cs446.backend.SupabaseClient
import com.example.cs446.backend.data.model.post.Post
import com.example.cs446.backend.data.repository.PostRepository
import com.example.cs446.backend.data.repository.UserRepository
import com.example.cs446.common.AppEvent
import com.example.cs446.common.EventBus
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class ProfileViewModel : ViewModel() {
    private val postRepository = PostRepository()
    private val userRepository = UserRepository()

    private val _avatarUrl = MutableStateFlow<String?>(null)
    private val _username = MutableStateFlow<String>("TEMP_USERNAME")
    private val _bio = MutableStateFlow<String>("TEMP_BIO")

    val avatar: StateFlow<String?> = _avatarUrl
    val username: StateFlow<String> = _username
    val bio: StateFlow<String> = _bio

    private val _allPosts = MutableStateFlow<List<Post>>(listOf())
    private val _numberOfFollowing = MutableStateFlow<Int>(0)

    val posts: StateFlow<List<Post>> = _allPosts
    val numberOfFollowing: StateFlow<Int> = _numberOfFollowing

    var isLoading = false;



    init {
        loadMorePosts()
        loadProfileInfo()
    }

    fun updateProfile(
        context: Context,
        avatarUri: Uri,
        username: String,
        bio: String
    ) {
        viewModelScope.launch {
            if (avatarUri != Uri.EMPTY) {
                userRepository.updateAvatar(context, avatarUri)
            }
            _username.value = username
            // userRepository.updateUsername(username)
            _bio.value = bio
        }

        loadProfileInfo()
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

    fun loadProfileInfo() {
//        if (isLoading) return
//        isLoading = true

        viewModelScope.launch {
            try {
                val userID = userRepository.getCurrentUserId()

                if (userID == null) {
                    throw Exception("userID = null")
                }
                val user = userRepository.getUserById(userID)

                if (user == null) {
                    throw Exception("user = null")
                }
                _avatarUrl.value = user.avatarUrl
                _username.value = user.username
                _bio.value = userRepository.getBioByUser(user)

                _numberOfFollowing.value = postRepository.getNumberOfPetsFollowing(userID)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }

            EventBus.events.collect { event ->
                when (event) {
                    is AppEvent.PetFollowed -> loadProfileInfo()
                    else -> null
                }
            }
        }
    }
}