package ru.training.pikabu.data.repository

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import ru.training.pikabu.data.model.Post

class PostRepository {
    private var nextId = 1

    fun getPosts(): Flow<List<Post>> = flow {
        val posts = mutableListOf<Post>()
        while (nextId < 15) {
            delay(3000)
            posts.add(Post(nextId.toString(), "New Post #$nextId"))
            emit(posts.toList())
            nextId++
        }
    }
}