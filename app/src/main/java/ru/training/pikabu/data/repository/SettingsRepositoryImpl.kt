package ru.training.pikabu.data.repository

import ru.training.pikabu.R
import ru.training.pikabu.data.api.ApiService
import ru.training.pikabu.data.model.LinkItem
import ru.training.pikabu.data.model.LinkType

class SettingsRepositoryImpl(private val apiService: ApiService) : SettingsRepository {
    private val internalLinks = mutableListOf<LinkItem>()
    private val externalLinks = mutableListOf<LinkItem>()
    private val customSettings = mutableListOf<LinkItem>()

    override suspend fun getInternalLinks(): List<LinkItem> {
        try {
            val links = apiService.getInternalLinks()
            internalLinks.clear()
            internalLinks.addAll(links)
        } catch (e: Exception) {
            internalLinks.addAll(
                listOf(
                    LinkItem("Комментарии дня", R.drawable.comment, LinkType.Internal),
                    LinkItem("О нас", R.drawable.circle_exclamation, LinkType.Internal),
                    LinkItem("Внешний вид", R.drawable.palette, LinkType.Internal)
                )
            )
        }
        return internalLinks
    }

    override suspend fun getExternalLinks(): List<LinkItem> {
        try {
            val links = apiService.getExternalLinks()
            externalLinks.clear()
            externalLinks.addAll(links)
        } catch (e: Exception) {
            externalLinks.addAll(
                listOf(
                    LinkItem("Кодекс Пикабу", R.drawable.pikabu_cake, LinkType.External),
                    LinkItem("Правила соцсети", R.drawable.megaphone, LinkType.External),
                    LinkItem("О рекомендациях", R.drawable.open_book, LinkType.External),
                    LinkItem("FAQ", R.drawable.circle_exclamation, LinkType.External),
                    LinkItem("Магазин", R.drawable.shop, LinkType.External),
                    LinkItem("Зал славы", R.drawable.prize, LinkType.External)
                )
            )
        }
        return externalLinks
    }

    override suspend fun getCustomSettings(): List<LinkItem> = customSettings
    override suspend fun addCustomSetting(setting: LinkItem) {
        customSettings.add(setting)
    }
}