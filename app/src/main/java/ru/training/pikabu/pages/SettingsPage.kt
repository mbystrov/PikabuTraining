package ru.training.pikabu.pages

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import ru.training.pikabu.R
import ru.training.pikabu.SettingsState
import ru.training.pikabu.SettingsViewModel
import ru.training.pikabu.Wish
import ru.training.pikabu.showToast
import ru.training.pikabu.ui.theme.PikabuDimensions

sealed class LinkType {
    data object Internal : LinkType()
    data object External : LinkType()
}

data class LinkItem(
    val text: String,
    val iconResource: Int,
    val type: LinkType
)

@Composable
fun SettingsPage(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.handleWish(Wish.LoadLinks)
    }

    Column {
        Header(text = "Еще")
        Log.d("MB", "isLoading сейчас:${state.isLoading}")
        if (state.isLoading) {
            Log.d("MB", "До значка загрузки")
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            Log.d("MB", "После значка загрузки")
        } else {
            SettingsContent(
                state = state,
                onLinkClick = { link -> showToast(context = context, message = link.text) },
                onAddSettingClick = { viewModel.handleWish(Wish.ShowAddSettingDialog) },
                onLinkToggle = { linkText ->
                    viewModel.handleWish(
                        Wish.ToggleSetting(
                            linkText
                        )
                    )
                }
            )
            if (state.isAddSettingDialogVisible) {
                AddSettingDialog(
                    onDismiss = { viewModel.handleWish(Wish.ShowAddSettingDialog) },
                    onConfirm = { text, iconResource ->
                        viewModel.handleWish(Wish.AddSetting(text, iconResource))
                    }
                )
            }
        }
    }

}

@Composable
fun Header(modifier: Modifier = Modifier, text: String) {
    Text(
        text = text,
        modifier = modifier
            .fillMaxWidth()
            .wrapContentWidth(Alignment.CenterHorizontally)
            .padding(PikabuDimensions.paddingMedium),
        style = MaterialTheme.typography.headlineMedium
    )
}

@Composable
fun SettingsContent(
    state: SettingsState,
    onLinkClick: (LinkItem) -> Unit,
    onAddSettingClick: () -> Unit,
    onLinkToggle: (String) -> Unit
) {
    LazyColumn {
        item {
            InternalLinksSection(
                links = state.internalLinks,
                selectedSettings = state.selectedLinksIds,
                onLinkClick = onLinkClick,
                onClickToggle = onLinkToggle
            )
        }
        item {
            ExternalLinksSection(
                links = state.externalLinks,
                selectedSettings = state.selectedLinksIds,
                onLinkClick = onLinkClick,
                onClickToggle = onLinkToggle
            )
        }
        item {
            CustomSettingsSection(
                settings = state.customSetting,
                selectedSettings = state.selectedLinksIds,
                onSettingClick = onLinkClick,
                onClickToggle = onLinkToggle
            )
        }
        item { AddSettingButton(onClick = onAddSettingClick) }
    }
}

@Composable
fun InternalLinksSection(
    links: List<LinkItem>,
    selectedSettings: Set<String>,
    onLinkClick: (LinkItem) -> Unit,
    onClickToggle: (String) -> Unit
) {
    Column(
        modifier = Modifier.padding(bottom = PikabuDimensions.paddingLarge)
    ) {
        links.forEach { link ->
            LinkItemComposable(
                item = link,
                isSelected = selectedSettings.contains(link.text),
                onClick = {
                    onClickToggle(link.text)
                    onLinkClick(link)
                })
            HorizontalDivider()
        }
    }
}

@Composable
fun ExternalLinksSection(
    modifier: Modifier = Modifier,
    links: List<LinkItem>,
    selectedSettings: Set<String>,
    onLinkClick: (LinkItem) -> Unit,
    onClickToggle: (String) -> Unit
) {
    Column(modifier = modifier) {
        links.forEach { link ->
            LinkItemComposable(
                item = link,
                isSelected = selectedSettings.contains(link.text),
                onClick = {
                    onClickToggle(link.text)
                    onLinkClick(link)
                })
        }
    }
}

@Composable
fun CustomSettingsSection(
    modifier: Modifier = Modifier,
    settings: List<LinkItem>,
    selectedSettings: Set<String>,
    onSettingClick: (LinkItem) -> Unit,
    onClickToggle: (String) -> Unit,
) {
    Column {
        settings.forEach { setting ->
            LinkItemComposable(
                item = setting,
                isSelected = selectedSettings.contains(setting.text),
                onClick = {
                    onClickToggle(setting.text)
                    onSettingClick(setting)
                })
            HorizontalDivider()
        }
    }
}

@Composable
fun AddSettingButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(PikabuDimensions.paddingMedium)
    ) {
        Log.d("MB", "Отображения текста кнопки")
        Text(text = "Добавить настройку")
    }
}

@Composable
fun LinkItemComposable(
    modifier: Modifier = Modifier,
    item: LinkItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(
                vertical = PikabuDimensions.paddingMedium,
                horizontal = PikabuDimensions.paddingMedium
            )
            .semantics { contentDescription = getContentDescription(item) }
            .background(
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    Color.Transparent
                }, shape = MaterialTheme.shapes.medium
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LeadingIcon(iconResource = item.iconResource)
        LinkText(text = item.text)
        Spacer(modifier = Modifier.weight(1f))
        TrailingIcon(type = item.type)
    }
}

@Composable
fun LeadingIcon(modifier: Modifier = Modifier, iconResource: Int) {
    Icon(
        painter = painterResource(id = iconResource),
        contentDescription = null,
        modifier = modifier.size(PikabuDimensions.iconSizeMedium)
    )
}

@Composable
fun LinkText(modifier: Modifier = Modifier, text: String) {
    Text(
        text = text,
        modifier = modifier
            .padding(PikabuDimensions.paddingMedium),
        style = MaterialTheme.typography.bodyMedium
    )
}

@Composable
fun TrailingIcon(modifier: Modifier = Modifier, type: LinkType) {
    val iconResource = getIconResource(type)
    Icon(
        painter = painterResource(id = iconResource),
        contentDescription = null,
        modifier = modifier.size(PikabuDimensions.iconSizeMedium)
    )
}

fun getIconResource(type: LinkType) = when (type) {
    is LinkType.Internal -> R.drawable.arrow
    is LinkType.External -> R.drawable.new_tab
}

fun getContentDescription(item: LinkItem) = when (item.type) {
    is LinkType.Internal -> "${item.text}, нажмите для открытия экрана"
    is LinkType.External -> "${item.text}, нажмите для перехода на сайт"
}

@Composable
fun AddSettingDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Int) -> Unit
) {
    var text by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableIntStateOf(R.drawable.android) }

    val icons = listOf(
        R.drawable.circle_exclamation,
        R.drawable.circle_question,
        R.drawable.shop,
        R.drawable.arrow,
        R.drawable.android,
        R.drawable.prize,
        R.drawable.palette,
        R.drawable.pikabu_cake
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Добавить настройку") },
        text = {
            Column {
                TextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Название настройки") }
                )
                Spacer(modifier = Modifier.height(PikabuDimensions.paddingMedium))
                Text("Выберите иконку:")
                LazyRow {
                    items(icons) { icon ->
                        IconButton(
                            onClick = { selectedIcon = icon }
                        ) {
                            Icon(
                                painter = painterResource(id = icon),
                                contentDescription = null,
                                tint = if (selectedIcon == icon) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(text, selectedIcon) }
            ) {
                Text("Добавить")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

//@Preview(showBackground = true)
//@Composable
//fun SettingsPagePreview() {
//    SettingsPage()
//}