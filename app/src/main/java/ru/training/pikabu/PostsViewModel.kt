package ru.training.pikabu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ru.training.pikabu.data.model.Post
import ru.training.pikabu.data.repository.PostRepository
import ru.training.pikabu.data.repository.PostRepositoryImpl
import java.util.UUID

class PostsViewModel : ViewModel() {
    private val postRepository: PostRepository = PostRepositoryImpl()
    private val _postsData = MutableStateFlow<List<Post>>(emptyList())
    val postsData: StateFlow<List<Post>> = _postsData


    fun createPost() {
        viewModelScope.launch {
            val newPost = Post(
                "${UUID.randomUUID()}",
                "Post ${_postsData.value.size + 1}"
            )
            postRepository.addPost(newPost)
            refreshPostsData()
        }
    }

    fun deletePost() {
        viewModelScope.launch {
            if (_postsData.value.isNotEmpty()) {
                postRepository.deletePost()
            }
            refreshPostsData()
        }
    }

    private fun refreshPostsData() {
        viewModelScope.launch {
            val posts = postRepository.getPosts()
            _postsData.value = posts
        }
    }
}