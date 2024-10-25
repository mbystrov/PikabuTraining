package ru.training.pikabu.pages

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import ru.training.pikabu.R
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
fun SettingsPage(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    Column {
        Header(text = "Еще")
        InternalLinksSection(context = context)
        ExternalLinksSection(context = context)
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
fun InternalLinksSection(modifier: Modifier = Modifier, context: Context) {
    Column(
        modifier = modifier.padding(bottom = PikabuDimensions.paddingLarge)
    ) {
        internalLinks.forEach { link ->
            LinkItem(modifier, link) {
                showToast(context, link.text, Toast.LENGTH_SHORT)
            }
            HorizontalDivider()
        }
    }
}

@Composable
fun ExternalLinksSection(modifier: Modifier = Modifier, context: Context) {
    Column(modifier = modifier) {
        externalLinks.forEach { link ->
            LinkItem(modifier, link) {
                showToast(context, link.text, Toast.LENGTH_SHORT)
            }
        }
    }
}

@Composable
fun LinkItem(modifier: Modifier = Modifier, item: LinkItem, onClick: () -> Unit) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(
                vertical = PikabuDimensions.paddingMedium,
                horizontal = PikabuDimensions.paddingMedium
            )
            .semantics { contentDescription = getContentDescription(item) },
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

val internalLinks = listOf(
    LinkItem("Комментарии дня", R.drawable.comment, LinkType.Internal),
    LinkItem("О нас", R.drawable.circle_exclamation, LinkType.Internal),
    LinkItem("Внешний вид", R.drawable.palette, LinkType.Internal)
)

val externalLinks = listOf(
    LinkItem("Кодекс Пикабу", R.drawable.pikabu_cake, LinkType.External),
    LinkItem("Правила соцсети", R.drawable.megaphone, LinkType.External),
    LinkItem("О рекомендациях", R.drawable.open_book, LinkType.External),
    LinkItem("FAQ", R.drawable.circle_exclamation, LinkType.External),
    LinkItem("Магазин", R.drawable.shop, LinkType.External),
    LinkItem("Зал славы", R.drawable.prize, LinkType.External)
)

@Preview(showBackground = true)
@Composable
fun SettingsPagePreview() {
    SettingsPage()
}