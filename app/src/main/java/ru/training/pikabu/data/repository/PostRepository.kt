package ru.training.pikabu.data.repository

import ru.training.pikabu.data.model.Post

interface PostRepository {
    suspend fun getPosts(): List<Post>
    suspend fun addPost(post: Post)
    suspend fun deletePost()
}