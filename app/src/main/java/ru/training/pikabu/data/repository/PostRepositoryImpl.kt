package ru.training.pikabu.data.repository

import ru.training.pikabu.data.model.Post

class PostRepositoryImpl: PostRepository {
    private val postsList = mutableListOf<Post>()

    override suspend fun getPosts(): List<Post> {
        return postsList.toList()
    }

    override suspend fun addPost(post: Post) {
        postsList.add(post)
    }

    override suspend fun deletePost() {
        postsList.removeFirst()
    }
}