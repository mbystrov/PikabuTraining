package ru.training.pikabu.data.repository

import ru.training.pikabu.data.model.Tag

interface TagRepository {
    suspend fun getTags(): Set<Tag>
    suspend fun createTag(tag: Tag)
    suspend fun deleteTag(tag: Tag)
}