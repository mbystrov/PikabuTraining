package ru.training.pikabu

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ru.training.pikabu.data.model.Post
import ru.training.pikabu.data.model.Tag
import ru.training.pikabu.data.repository.PostRepository
import ru.training.pikabu.data.repository.PostRepositoryImpl
import ru.training.pikabu.data.repository.TagRepository
import ru.training.pikabu.data.repository.TagRepositoryImpl
import java.util.UUID

class PostsViewModel : ViewModel() {
    private val postRepository: PostRepository = PostRepositoryImpl()
    private val _postsData = MutableStateFlow<List<Post>>(emptyList())
    val postsData: StateFlow<List<Post>> = _postsData

    private val tagRepository: TagRepository = TagRepositoryImpl()
    private val _tagsData = MutableStateFlow<Set<Tag>>(emptySet())
    val tagsData: StateFlow<Set<Tag>> = _tagsData

    private val _selectedTags = MutableStateFlow<Set<String>>(emptySet())
    val selectedTags: StateFlow<Set<String>> = _selectedTags

    private val _tagText = MutableStateFlow("")
    val tagText: StateFlow<String> = _tagText

    init {
        Log.e("MB", "VM created")
    }

    override fun onCleared() {
        Log.e("MB", "VM cleared")
        super.onCleared()
    }

    fun updateTagText(text: String) {
        _tagText.value = text
    }

    fun toggleTag(tagValue: String) {
        viewModelScope.launch {
            _selectedTags.value = _selectedTags.value.toMutableSet()
                .apply { if (contains(tagValue)) remove(tagValue) else add(tagValue) }
            refreshPostsData()
        }
    }

    fun createTag(tagValue: String) {
        viewModelScope.launch {
            val newTag = Tag(
                tagValue = tagValue
            )
            tagRepository.createTag(newTag)
            refreshTagsData()
            _tagText.value = ""
        }
    }

    fun deleteTag(tagValue: String) {
        viewModelScope.launch {
            val tag = Tag(tagValue = tagValue)
            tagRepository.deleteTag(tag)
            _selectedTags.value -= tagValue
            _tagsData.value = _tagsData.value.toMutableSet() - tag
            refreshTagsData()
        }
    }

    private fun refreshTagsData() {
        viewModelScope.launch {
            val tags = tagRepository.getTags()
            _tagsData.value = tags
        }
    }

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