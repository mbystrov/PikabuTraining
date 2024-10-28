package ru.training.pikabu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import ru.training.pikabu.data.model.Post
import ru.training.pikabu.data.repository.PostRepository

class PostsViewModel : ViewModel() {
    private val postRepository = PostRepository()
    private val _postsData = MutableSharedFlow<List<Post>>()
    val postsData: SharedFlow<List<Post>> = _postsData.asSharedFlow()

    private var job: Job? = null

    init {
        refreshPostsData()
    }

    private fun refreshPostsData() {
        job?.cancel()
        job = viewModelScope.launch {
            postRepository.getPosts().collect { posts ->
                _postsData.emit(posts)
            }
        }
    }
}