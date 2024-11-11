package ru.training.pikabu.data.model

import kotlinx.serialization.Serializable

@Serializable
data class LinkItem(
    val text: String,
    val iconResource: Int,
    val type: LinkType
)