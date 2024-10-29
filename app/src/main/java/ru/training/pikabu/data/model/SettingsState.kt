package ru.training.pikabu.data.model

import ru.training.pikabu.pages.LinkItem

data class SettingsState(
    val internalLinks: List<LinkItem> = emptyList(),
    val externalLinks: List<LinkItem> = emptyList(),
    val customSetting: List<LinkItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
