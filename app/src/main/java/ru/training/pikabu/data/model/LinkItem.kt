package ru.training.pikabu.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "links")
@Serializable
data class LinkItem(
    @PrimaryKey val text: String,
    val iconResource: Int,
    val type: LinkType
)