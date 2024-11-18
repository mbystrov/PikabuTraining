package ru.training.pikabu

import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
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
import ru.training.pikabu.data.db.AppDatabase
import ru.training.pikabu.data.model.LinkItem
import ru.training.pikabu.data.model.LinkType
import ru.training.pikabu.data.repository.LinksRepository
import ru.training.pikabu.data.repository.LinksRepositoryImpl

/* Это вариант MVI с использованием Flow в акторе, т.о. точка входа остаётся она - handleWish() и действия из неё передаются в actor и reducer */
class LinksViewModel(application: PikabuApplication) : AndroidViewModel(application) {
    private val appDatabase = AppDatabase.getDatabase(application)
    private val linkItemDao = appDatabase.linkItemDao()
    private val repository: LinksRepository =
        LinksRepositoryImpl(RetrofitClient.apiService, linkItemDao)
    private val _state = MutableStateFlow(LinksState())
    val state: StateFlow<LinksState> = _state.asStateFlow()
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

    class ActorImpl(private val repository: LinksRepository) : Actor {
        override suspend fun invoke(state: LinksState, wish: Wish): Flow<Effect> = flow {
            when (wish) {
                is Wish.LoadLinks -> emitAll(loadLinks())
                is Wish.ShowAddCustomLinkDialog -> emit(Effect.DialogVisibilityChanged(!state.isAddCustomLinkDialogVisible))
                is Wish.AddCustomLink -> emitAll(addCustomLink(wish.text, wish.iconResource))
                is Wish.UpdateAddCustomLinkDialogText -> emit(Effect.AddCustomLinkDialogTextUpdated(wish.text))
                is Wish.ToggleLink -> emit(toggleCustomLink(state, wish.linkText))
            }
        }

        private suspend fun loadLinks(): Flow<Effect> = flow {
            try {
                emit(Effect.StartedLoading)
                emit(
                    Effect.LinksLoaded(
                        internalLinks = repository.getInternalLinks(),
                        externalLinks = repository.getExternalLinks(),
                        customLinks = repository.getCustomLinks()
                    )
                )
            } catch (e: Exception) {
                emit(Effect.Error(e.message ?: "Неизвестная ошибка"))
            }
        }

        private suspend fun addCustomLink(text: String, iconResource: Int): Flow<Effect> = flow {
            try {
                require(text.isNotEmpty()) { "Текст настройки не может быть пустым" }
                val newCustomLink =
                    LinkItem(text = text, iconResource = iconResource, type = LinkType.Custom)
                repository.addCustomLink(newCustomLink)
                val updatedCustomLinks = repository.getCustomLinks()
                emit(Effect.CustomLinkAdded(updatedCustomLinks))
            } catch (e: Exception) {
                emit(Effect.Error(e.message ?: "Ошибка добавления настройки"))
            }
        }

        private fun toggleCustomLink(state: LinksState, linkText: String): Effect {
            val newSelectedLinkIds = if (state.selectedLinksIds.contains(linkText)) {
                state.selectedLinksIds - linkText
            } else {
                state.selectedLinksIds + linkText
            }
            return Effect.LinkToggled(newSelectedLinkIds)
        }
    }

    class ReducerImpl : Reducer {
        override fun invoke(state: LinksState, effect: Effect): LinksState = when (effect) {
            is Effect.StartedLoading -> state.copy(isLoading = true, error = null)
            is Effect.LinksLoaded -> state.copy(
                internalLinks = effect.internalLinks,
                externalLinks = effect.externalLinks,
                customLinks = effect.customLinks,
                isLoading = false,
                error = null
            )

            is Effect.Error -> state.copy(error = effect.errorMessage, isLoading = false)
            is Effect.CustomLinkAdded -> state.copy(
                customLinks = effect.customLinks,
                isAddCustomLinkDialogVisible = false,
                error = null
            )

            is Effect.LinkToggled -> state.copy(
                selectedLinksIds = effect.selectedLinksIds,
                error = null
            )

            is Effect.AddCustomLinkDialogTextUpdated -> state.copy(addCustomLinkDialogText = effect.text)

            is Effect.DialogVisibilityChanged -> state.copy(
                isAddCustomLinkDialogVisible = effect.isVisible,
                error = null
            )

        }
    }

    class NewsPublisherImpl() : NewsPublisher {
        override fun invoke(wish: Wish, effect: Effect, state: LinksState): News? {
            return when {
                effect is Effect.LinkToggled && wish is Wish.ToggleLink -> {
                    val isSelected = state.selectedLinksIds.contains(wish.linkText)
                    val status = if (isSelected) "выбран" else "отключён"
                    Log.d("MB", "status: $status")
                    News.ShowToast("Элемент ${wish.linkText} $status")
                }

                else -> null
            }
        }

    }

    companion object {
        fun factory(application: PikabuApplication): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                LinksViewModel(application)
            }
        }
    }
}

typealias Actor = suspend (state: LinksState, wish: Wish) -> Flow<Effect>

typealias Reducer = (state: LinksState, effect: Effect) -> LinksState

typealias NewsPublisher =
            (wish: Wish, effect: Effect, state: LinksState) -> News?

sealed class Wish {
    data object LoadLinks : Wish()
    data object ShowAddCustomLinkDialog : Wish()
    data class AddCustomLink(val text: String, val iconResource: Int) : Wish()
    data class UpdateAddCustomLinkDialogText(val text: String) : Wish()
    data class ToggleLink(val linkText: String) : Wish()
}

sealed class Effect {
    data object StartedLoading : Effect()
    data class LinksLoaded(
        val internalLinks: List<LinkItem>,
        val externalLinks: List<LinkItem>,
        val customLinks: List<LinkItem>
    ) :
        Effect()

    data class Error(val errorMessage: String) : Effect()
    data class CustomLinkAdded(val customLinks: List<LinkItem>) : Effect()
    data class LinkToggled(val selectedLinksIds: Set<String>) : Effect()
    data class AddCustomLinkDialogTextUpdated(val text: String) : Effect()
    data class DialogVisibilityChanged(val isVisible: Boolean) : Effect()
}

sealed class News {
    data class ShowToast(val toastMessage: String) : News()
}

data class LinksState(
    val internalLinks: List<LinkItem> = emptyList(),
    val externalLinks: List<LinkItem> = emptyList(),
    val customLinks: List<LinkItem> = emptyList(),
    val selectedLinksIds: Set<String> = emptySet(),
    val addCustomLinkDialogText: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isAddCustomLinkDialogVisible: Boolean = false
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