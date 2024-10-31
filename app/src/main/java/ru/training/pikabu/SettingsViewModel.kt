package ru.training.pikabu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.training.pikabu.data.repository.SettingsRepository
import ru.training.pikabu.data.repository.SettingsRepositoryImpl
import ru.training.pikabu.pages.LinkItem
import ru.training.pikabu.pages.LinkType

class SettingsViewModel : ViewModel() {
    private val repository: SettingsRepository = SettingsRepositoryImpl()
    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    fun handleIntent(intent: SettingsIntent) {
        when (intent) {
            is SettingsIntent.LoadLinks -> loadLinks()
            is SettingsIntent.AddSetting -> handleAddSettingButtonClick(
                intent.text,
                intent.iconResource
            )

            is SettingsIntent.ToggleSetting -> handleToggleSetting(intent.linkText)
            is SettingsIntent.ShowAddSettingDialog -> showAddSettingDialog()
        }
    }

    private fun loadLinks() {
        viewModelScope.launch {
            setState { copy(isLoading = true) }
            try {
                val internalLinks = repository.getInternalLinks()
                val externalLinks = repository.getExternalLinks()
                setState {
                    copy(
                        internalLinks = internalLinks,
                        externalLinks = externalLinks,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                setState {
                    copy(
                        error = e.message ?: "Неизвестная ошибка"
                    )
                }
            }
        }
    }

    private fun handleAddSettingButtonClick(text: String, iconResource: Int) {
        viewModelScope.launch {
            val newCustomSetting = LinkItem(
                text = text,
                iconResource = iconResource,
                type = LinkType.Internal
            )
            repository.addCustomSetting(newCustomSetting)
            val updatedCustomSettings = repository.getCustomSettings()
            setState {
                copy(
                    customSetting = updatedCustomSettings,
                    isAddSettingDialogVisible = false
                )
            }
        }
    }

    private fun handleToggleSetting(linkId: String) {
        viewModelScope.launch {
            setState {
                val newSelectedLinkIds = if (selectedLinksIds.contains(linkId)) {
                    selectedLinksIds - linkId
                } else {
                    selectedLinksIds + linkId
                }
                copy(selectedLinksIds = newSelectedLinkIds)
            }
        }
    }

    private fun showAddSettingDialog() = setState {
        if (isAddSettingDialogVisible) {
            copy(isAddSettingDialogVisible = false)
        } else {
            copy(
                isAddSettingDialogVisible = true
            )
        }
    }

    private fun setState(reducer: SettingsState.() -> SettingsState) {
        _state.value = _state.value.reducer()
    }
}

sealed interface SettingsIntent {
    data object LoadLinks : SettingsIntent
    data object ShowAddSettingDialog : SettingsIntent
    data class AddSetting(val text: String, val iconResource: Int) : SettingsIntent
    data class ToggleSetting(val linkText: String) : SettingsIntent
}

data class SettingsState(
    val internalLinks: List<LinkItem> = emptyList(),
    val externalLinks: List<LinkItem> = emptyList(),
    val customSetting: List<LinkItem> = emptyList(),
    val selectedLinksIds: Set<String> = emptySet(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isAddSettingDialogVisible: Boolean = false
)