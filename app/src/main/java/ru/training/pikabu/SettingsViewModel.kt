package ru.training.pikabu

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.training.pikabu.data.api.RetrofitClient
import ru.training.pikabu.data.model.LinkItem
import ru.training.pikabu.data.model.LinkType
import ru.training.pikabu.data.repository.SettingsRepository
import ru.training.pikabu.data.repository.SettingsRepositoryImpl

/* Это вариант MVI с использованием Flow в акторе, т.о. точка входа остаётся она - handleWish() и действия из неё передаются в actor и reducer */
class SettingsViewModel : ViewModel() {
    private val repository: SettingsRepository =
        SettingsRepositoryImpl(RetrofitClient.apiService)
    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()
    private val _news = MutableSharedFlow<News>()
    val news: SharedFlow<News> = _news.asSharedFlow()

    private val actor: Actor = ActorImpl(repository)
    private val reducer: Reducer = ReducerImpl()
    private val newsPublisher: NewsPublisher = NewsPublisherImpl()

    init {
        handleWish(Wish.LoadLinks)
    }

    fun handleWish(wish: Wish) {
        viewModelScope.launch {
            try {
                actor.invoke(state.value, wish).collect { effect ->
                    val newState = reducer.invoke(state.value, effect)
                    _state.update { newState }
                    newsPublisher.invoke(wish, effect, newState)?.let { news ->
                        _news.emit(news)
                    }
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message ?: "Неизвестная ошибка") }
            }
        }
    }

    class ActorImpl(private val repository: SettingsRepository) : Actor {
        override suspend fun invoke(state: SettingsState, wish: Wish): Flow<Effect> = flow {
            when (wish) {
                is Wish.LoadLinks -> emitAll(loadLinks())
                is Wish.ShowAddSettingDialog -> emit(Effect.DialogVisibilityChanged(!state.isAddSettingDialogVisible))
                is Wish.AddSetting -> emitAll(addSetting(wish.text, wish.iconResource))
                is Wish.UpdateAddSettingDialogText -> emit(Effect.AddSettingDialogTextUpdated(wish.text))
                is Wish.ToggleSetting -> emit(toggleSetting(state, wish.linkText))
            }
        }

        private suspend fun loadLinks(): Flow<Effect> = flow {
            try {
                emit(Effect.StartedLoading)
                delay(4000)
                emit(
                    Effect.LinksLoaded(
                        internalLinks = repository.getInternalLinks(),
                        externalLinks = repository.getExternalLinks()
                    )
                )
            } catch (e: Exception) {
                emit(Effect.Error(e.message ?: "Неизвестная ошибка"))
            }
        }

        private suspend fun addSetting(text: String, iconResource: Int): Flow<Effect> = flow {
            try {
                require(text.isNotEmpty()) { "Текст настройки не может быть пустым" }
                val newCustomSetting =
                    LinkItem(text = text, iconResource = iconResource, type = LinkType.Internal)
                repository.addCustomSetting(newCustomSetting)
                val updatedCustomSettings = repository.getCustomSettings()
                emit(Effect.SettingAdded(updatedCustomSettings))
            } catch (e: Exception) {
                emit(Effect.Error(e.message ?: "Ошибка добавления настройки"))
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

            is Effect.AddSettingDialogTextUpdated -> state.copy(addSettingDialogText = effect.text)

            is Effect.DialogVisibilityChanged -> state.copy(
                isAddSettingDialogVisible = effect.isVisible,
                error = null
            )

        }
    }

    class NewsPublisherImpl() : NewsPublisher {
        override fun invoke(wish: Wish, effect: Effect, state: SettingsState): News? {
            return when {
                effect is Effect.SettingToggled && wish is Wish.ToggleSetting -> {
                    val isSelected = state.selectedLinksIds.contains(wish.linkText)
                    val status = if (isSelected) "выбран" else "отключён"
                    Log.d("MB", "status: $status")
                    News.ShowToast("Элемент ${wish.linkText} $status")
                }

                else -> null
            }
        }

    }

}

typealias Actor = suspend (state: SettingsState, wish: Wish) -> Flow<Effect>

typealias Reducer = (state: SettingsState, effect: Effect) -> SettingsState

typealias NewsPublisher =
            (wish: Wish, effect: Effect, state: SettingsState) -> News?

sealed class Wish {
    data object LoadLinks : Wish()
    data object ShowAddSettingDialog : Wish()
    data class AddSetting(val text: String, val iconResource: Int) : Wish()
    data class UpdateAddSettingDialogText(val text: String) : Wish()
    data class ToggleSetting(val linkText: String) : Wish()
}

sealed class Effect {
    data object StartedLoading : Effect()
    data class LinksLoaded(val internalLinks: List<LinkItem>, val externalLinks: List<LinkItem>) :
        Effect()

    data class Error(val errorMessage: String) : Effect()
    data class SettingAdded(val customSettings: List<LinkItem>) : Effect()
    data class SettingToggled(val selectedLinksIds: Set<String>) : Effect()
    data class AddSettingDialogTextUpdated(val text: String) : Effect()
    data class DialogVisibilityChanged(val isVisible: Boolean) : Effect()
}

sealed class News {
    data class ShowToast(val toastMessage: String) : News()
}

data class SettingsState(
    val internalLinks: List<LinkItem> = emptyList(),
    val externalLinks: List<LinkItem> = emptyList(),
    val customSetting: List<LinkItem> = emptyList(),
    val selectedLinksIds: Set<String> = emptySet(),
    val addSettingDialogText: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isAddSettingDialogVisible: Boolean = false
)

/* Это вариант MVI, где также используется Flow и одна точка входа - handleWish(), однако действия из неё передаются только в actor, который сам передаёт effect и state в reducer.
class SettingsViewModel : ViewModel() {
    private val repository: SettingsRepository = SettingsRepositoryImpl()
    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()
    private val _news = MutableSharedFlow<News>()
    val news: SharedFlow<News> = _news.asSharedFlow()

    private val reducer: Reducer = ReducerImpl()
    private val actor: Actor = ActorImpl(repository, reducer)
    private val newsPublisher: NewsPublisher = NewsPublisherImpl()

    init {
        handleWish(Wish.LoadLinks)
    }

    fun handleWish(wish: Wish) {
        viewModelScope.launch {
            try {
                actor.invoke(state.value, wish).collect { (newState, effect) ->
                    _state.update { newState }
                    newsPublisher.invoke(wish, effect, newState)?.let { news ->
                        _news.emit(news)
                    }
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message ?: "Неизвестная ошибка") }
            }
        }
    }

    class ActorImpl(private val repository: SettingsRepository, val reducer: Reducer) : Actor {
        override suspend fun invoke(state: SettingsState, wish: Wish): Flow<Pair<SettingsState, Effect>> =
            when (wish) {
                is Wish.LoadLinks -> loadLinks(state)
                is Wish.ShowAddSettingDialog -> showAddSettingDialog(state)
                is Wish.AddSetting -> addSetting(wish.text, wish.iconResource, state)
                is Wish.ToggleSetting -> toggleSetting(state, wish.linkText)
            }

        private suspend fun loadLinks(state: SettingsState): Flow<Pair<SettingsState, Effect>> = flow {
            val startedLoadingEffect = Effect.StartedLoading
            val loadingState = reducer.invoke(state, startedLoadingEffect)
            emit(loadingState to startedLoadingEffect)
            try {
                delay(4000)
                val effect = Effect.LinksLoaded(
                    internalLinks = repository.getInternalLinks(),
                    externalLinks = repository.getExternalLinks()
                )
                emit(reducer.invoke(loadingState, effect) to effect)
            } catch (e: Exception) {
                val effect = Effect.Error(e.message ?: "Неизвестная ошибка")
                emit(reducer.invoke(loadingState, effect) to effect)
            }
        }


        private fun showAddSettingDialog(state: SettingsState): Flow<Pair<SettingsState, Effect>> = flow {
            val effect = Effect.DialogVisibilityChanged(!state.isAddSettingDialogVisible)
            emit(reducer.invoke(state, effect) to effect)
        }

        private suspend fun addSetting(
            text: String,
            iconResource: Int,
            state: SettingsState
        ): Flow<Pair<SettingsState, Effect>> = flow {
            try {
                require(text.isNotEmpty()) { "Текст настройки не может быть пустым" }
                val newCustomSetting =
                    LinkItem(text = text, iconResource = iconResource, type = LinkType.Internal)
                repository.addCustomSetting(newCustomSetting)
                val updatedCustomSettings = repository.getCustomSettings()
                val effect = Effect.SettingAdded(updatedCustomSettings)
                emit(reducer(state, effect) to effect)
            } catch (e: Exception) {
                val effect = Effect.Error(e.message ?: "Ошибка добавления настройки")
                emit(reducer.invoke(state, effect) to effect)
            }
        }

        private fun toggleSetting(
            state: SettingsState,
            linkText: String
        ): Flow<Pair<SettingsState, Effect>> = flow {
            val newSelectedLinkIds = if (state.selectedLinksIds.contains(linkText)) {
                state.selectedLinksIds - linkText
            } else {
                state.selectedLinksIds + linkText
            }
            val effect = Effect.SettingToggled(newSelectedLinkIds)
            emit(reducer.invoke(state, effect) to effect)
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

    class NewsPublisherImpl() : NewsPublisher {
        override fun invoke(wish: Wish, effect: Effect, state: SettingsState): News? {
            return when {
                effect is Effect.SettingToggled && wish is Wish.ToggleSetting -> {
                    val isSelected = state.selectedLinksIds.contains(wish.linkText)
                    val status = if (isSelected) "выбран" else "отключён"
                    Log.d("MB","status: $status")
                    News.ShowToast("Элемент ${wish.linkText} $status")
                }

                else -> null
            }
        }

    }

}

typealias Actor = suspend (state: SettingsState, wish: Wish) -> Flow<Pair<SettingsState, Effect>>

typealias Reducer = (state: SettingsState, effect: Effect) -> SettingsState

typealias NewsPublisher =
            (wish: Wish, effect: Effect, state: SettingsState) -> News?

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

sealed class News {
    data class ShowToast(val toastMessage: String) : News()
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
 */