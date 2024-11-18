package ru.training.pikabu.data.repository

import ru.training.pikabu.data.api.ApiService
import ru.training.pikabu.data.dao.LinkItemDao
import ru.training.pikabu.data.model.LinkItem
import ru.training.pikabu.data.model.LinkType

class LinksRepositoryImpl(
    private val apiService: ApiService,
    private val linkItemDao: LinkItemDao
) : LinksRepository {

    override suspend fun getInternalLinks(): List<LinkItem> = getLinks(LinkType.Internal)

    override suspend fun getExternalLinks(): List<LinkItem> = getLinks(LinkType.External)

    override suspend fun getCustomLinks(): List<LinkItem> = getLinks(LinkType.Custom)

    private suspend fun getLinks(linkType: LinkType): List<LinkItem> {
        return try {
            val links = when(linkType) {
                LinkType.Internal -> apiService.getInternalLinks()
                LinkType.External -> apiService.getExternalLinks()
                LinkType.Custom -> apiService.getCustomLinks()
            }
            linkItemDao.deleteAllLinks()
            linkItemDao.addLinks(links)
            links
        } catch (e: Exception) {
            linkItemDao.getAllLinksByType(linkType.toString())
        }
    }

    override suspend fun addCustomLink(linkItem: LinkItem) {
        linkItemDao.addLink(linkItem)
    }
}