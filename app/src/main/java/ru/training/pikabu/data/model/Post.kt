package ru.training.pikabu.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "posts")
@Serializable
data class Post(
    @PrimaryKey val id: Int,
    val name: String,
)