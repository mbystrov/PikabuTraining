package ru.training.pikabu

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.training.pikabu.data.model.SettingsState
import ru.training.pikabu.data.repository.SettingsRepository
import ru.training.pikabu.data.repository.SettingsRepositoryImpl
import ru.training.pikabu.pages.LinkItem
import ru.training.pikabu.pages.LinkType
import ru.training.pikabu.pages.externalLinks
import ru.training.pikabu.pages.internalLinks

class SettingsViewModel : ViewModel() {
    private val repository: SettingsRepository = SettingsRepositoryImpl()
    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    fun handleIntent(intent: SettingsIntent) {
        when (intent) {
            is SettingsIntent.LoadLinks -> loadLinks()
            is SettingsIntent.AddSetting -> handleAddSettingButtonClick()
        }
    }

    private fun loadLinks() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val internalLinks = repository.getInternalLinks()
                val externalLinks = repository.getExternalLinks()
                _state.update {
                    it.copy(
                        internalLinks = internalLinks,
                        externalLinks = externalLinks,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    private fun handleAddSettingButtonClick() {
        println("Log")
        Log.e("as", "text")
        viewModelScope.launch {
            val newCustomSetting = LinkItem(
                text = "Custom setting ${_state.value.customSetting.size + 1}",
                iconResource = R.drawable.android,
                type = LinkType.Internal
            )
            repository.addCustomSetting(newCustomSetting)
            val updatedCustomSettings = repository.getCustomSettings()
            _state.update { it.copy(customSetting = updatedCustomSettings) }
        }
    }
}

sealed interface SettingsIntent {
    data object LoadLinks : SettingsIntent
    data object AddSetting : SettingsIntent
}