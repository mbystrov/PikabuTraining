package ru.training.pikabu.data.repository

import ru.training.pikabu.pages.LinkItem

interface SettingsRepository {
    suspend fun getInternalLinks(): List<LinkItem>
    suspend fun getExternalLinks(): List<LinkItem>
    suspend fun getCustomSettings(): List<LinkItem>
    suspend fun addCustomSetting(setting: LinkItem)
}