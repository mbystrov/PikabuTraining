package ru.training.pikabu.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Post(
    val id: Int,
    val name: String,
)