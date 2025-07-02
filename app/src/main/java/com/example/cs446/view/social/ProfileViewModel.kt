package com.example.cs446.view.social

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cs446.backend.data.model.Post
import com.example.cs446.backend.data.repository.PostRepository
import com.example.cs446.backend.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {
    private val _allPosts = MutableStateFlow<List<Post>>(listOf())

    val posts: StateFlow<List<Post>> = _allPosts

    var isLoading = false;

    private val postRepository = PostRepository()
    private val userRepository = UserRepository()

    init {
        loadMorePosts()
    }

    fun loadMorePosts() {
        if (isLoading) return
        isLoading = true

        viewModelScope.launch {
            try {
                val newPosts = postRepository.loadProfilePosts(
                    userID = userRepository.getCurrentUserId()
                )
                println(newPosts[0].petImageUrl)
                _allPosts.value = (_allPosts.value + newPosts).sortedByDescending {
                    it.createdAt
                }.toMutableList()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }
}