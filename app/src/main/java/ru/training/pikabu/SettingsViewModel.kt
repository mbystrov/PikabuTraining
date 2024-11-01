package ru.training.pikabu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.training.pikabu.data.repository.SettingsRepository
import ru.training.pikabu.data.repository.SettingsRepositoryImpl
import ru.training.pikabu.pages.LinkItem
import ru.training.pikabu.pages.LinkType

class SettingsViewModel : ViewModel() {
    private val repository: SettingsRepository = SettingsRepositoryImpl()
    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    private val actor: Actor = ActorImpl(repository)
    private val reducer: Reducer = ReducerImpl()

    init {
        handleWish(Wish.LoadLinks)
    }

    fun handleWish(wish: Wish) {
        viewModelScope.launch {
            try {
                when (wish) {
                    is Wish.LoadLinks -> {
                        _state.update { reducer.invoke(it, Effect.StartedLoading) }
                        val effect = actor.invoke(state.value, wish)
                        _state.update { reducer.invoke(it, effect) }
                    }
                    else -> {
                        val effect = actor.invoke(state.value, wish)
                        _state.update { reducer.invoke(it, effect) }
                    }
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message ?: "Неизвестная ошибка") }
            }
        }
    }

    class ActorImpl(private val repository: SettingsRepository) : Actor {
        override suspend fun invoke(state: SettingsState, wish: Wish): Effect = when (wish) {
            is Wish.LoadLinks -> loadLinks()
            is Wish.ShowAddSettingDialog -> Effect.DialogVisibilityChanged(!state.isAddSettingDialogVisible)
            is Wish.AddSetting -> addSetting(wish.text, wish.iconResource)
            is Wish.ToggleSetting -> toggleSetting(state, wish.linkText)
        }

        private suspend fun loadLinks(): Effect =
            try {
                delay(300)
                Effect.LinksLoaded(
                    internalLinks = repository.getInternalLinks(),
                    externalLinks = repository.getExternalLinks()
                )
            } catch (e: Exception) {
                Effect.Error(e.message ?: "Неизвестная ошибка")
            }

        private suspend fun addSetting(text: String, iconResource: Int): Effect {
            return try {
                require(text.isNotEmpty()) { "Текст настройки не может быть пустым" }
                val newCustomSetting =
                    LinkItem(text = text, iconResource = iconResource, type = LinkType.Internal)
                repository.addCustomSetting(newCustomSetting)
                val updatedCustomSettings = repository.getCustomSettings()
                return Effect.SettingAdded(updatedCustomSettings)
            } catch (e: Exception) {
                Effect.Error(e.message ?: "Ошибка добавления настройки")
            }
        }

        private fun toggleSetting(state: SettingsState, linkText: String): Effect {
            val newSelectedLinkIds = if (state.selectedLinksIds.contains(linkText)) {
                state.selectedLinksIds - linkText
            } else {
                state.selectedLinksIds + linkText
            }
            return Effect.SettingToggled(newSelectedLinkIds)
        }

    }

    class ReducerImpl : Reducer {
        override fun invoke(state: SettingsState, effect: Effect): SettingsState = when (effect) {
            is Effect.StartedLoading -> state.copy(isLoading = true, error = null)
            is Effect.LinksLoaded -> state.copy(
                internalLinks = effect.internalLinks,
                externalLinks = effect.externalLinks,
                isLoading = false,
                error = null
            )

            is Effect.Error -> state.copy(error = effect.errorMessage, isLoading = false)
            is Effect.SettingAdded -> state.copy(
                customSetting = effect.customSettings,
                isAddSettingDialogVisible = false,
                error = null
            )

            is Effect.SettingToggled -> state.copy(
                selectedLinksIds = effect.selectedLinksIds,
                error = null
            )

            is Effect.DialogVisibilityChanged -> state.copy(
                isAddSettingDialogVisible = effect.isVisible,
                error = null
            )
        }
    }

}

typealias Actor = suspend (state: SettingsState, wish: Wish) -> Effect

typealias Reducer = (state: SettingsState, effect: Effect) -> SettingsState

sealed class Wish {
    data object LoadLinks : Wish()
    data object ShowAddSettingDialog : Wish()
    data class AddSetting(val text: String, val iconResource: Int) : Wish()
    data class ToggleSetting(val linkText: String) : Wish()
}

sealed class Effect {
    data object StartedLoading : Effect()
    data class LinksLoaded(val internalLinks: List<LinkItem>, val externalLinks: List<LinkItem>) :
        Effect()

    data class Error(val errorMessage: String) : Effect()
    data class SettingAdded(val customSettings: List<LinkItem>) : Effect()
    data class SettingToggled(val selectedLinksIds: Set<String>) : Effect()
    data class DialogVisibilityChanged(val isVisible: Boolean) : Effect()
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