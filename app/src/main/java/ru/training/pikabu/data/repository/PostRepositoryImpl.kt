package ru.training.pikabu.data.repository

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.training.pikabu.data.api.ApiService
import ru.training.pikabu.data.dao.PostDao
import ru.training.pikabu.data.model.Post

class PostRepositoryImpl(
    private val apiService: ApiService,
    private val postDao: PostDao
) : PostRepository {

    override suspend fun getPosts(): List<Post> {
        return try {
            val posts = apiService.getPosts()
            postDao.deleteAllPosts()
            postDao.addPosts(posts)
            posts
        } catch (e: Exception) {
            Log.d("MB", "Exception '${e.message}' caught")
            postDao.getAllPosts()
        }
    }

    override suspend fun addPost(post: Post) {
        postDao.addPost(post)
    }

    override suspend fun deletePost() {
        val posts = postDao.getAllPosts()
        if (posts.isNotEmpty()) {
            val updatedPosts = posts.drop(1)
            postDao.deleteAllPosts()
            postDao.addPosts(updatedPosts)
        }
    }
}