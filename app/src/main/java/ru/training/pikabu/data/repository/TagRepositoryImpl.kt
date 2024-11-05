package ru.training.pikabu.data.repository

import ru.training.pikabu.data.model.Tag

class TagRepositoryImpl : TagRepository {
    private val tagList = mutableSetOf<Tag>()

    override suspend fun getTags(): Set<Tag> = tagList.toSet()

    override suspend fun createTag(tag: Tag) {
        tagList.add(tag)
    }

    override suspend fun deleteTag(tag: Tag) {
        tagList -= tag
    }

}