package ru.training.pikabu.data.api

import retrofit2.http.GET
import ru.training.pikabu.data.model.LinkItem
import ru.training.pikabu.data.model.Post

interface ApiService {

    @GET("posts")
    suspend fun getPosts(): List<Post>
    @GET("external_links")
    suspend fun getExternalLinks(): List<LinkItem>
    @GET("internal_links")
    suspend fun getInternalLinks(): List<LinkItem>
}