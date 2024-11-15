package ru.training.pikabu.data.repository

import android.util.Log
import ru.training.pikabu.data.api.ApiService
import ru.training.pikabu.data.model.Post

class PostRepositoryImpl(private val apiService: ApiService): PostRepository {
    private val postsList = mutableListOf<Post>()

    override suspend fun getPosts(): List<Post> {
        return try {
            val posts = apiService.getPosts()
            postsList.clear()
            postsList.addAll(posts)
            posts
        } catch (e: Exception) {
            Log.d("MB", "Exception '${e.message}' caught")
            postsList.toList()
        }
    }

    override suspend fun addPost(post: Post) {
        postsList.add(post)
    }

    override suspend fun deletePost() {
        postsList.removeFirst()
    }
}