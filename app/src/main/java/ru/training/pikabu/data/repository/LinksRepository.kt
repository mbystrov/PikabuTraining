package ru.training.pikabu.data.repository

import ru.training.pikabu.data.model.LinkItem

interface LinksRepository {
    suspend fun getInternalLinks(): List<LinkItem>
    suspend fun getExternalLinks(): List<LinkItem>
    suspend fun getCustomLinks(): List<LinkItem>
    suspend fun addCustomLink(linkItem: LinkItem)
}