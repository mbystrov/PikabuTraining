package ru.training.pikabu

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.training.pikabu.ui.theme.PikabuTrainingTheme

private var currentToast: Toast? = null

fun showToast(context: Context, message: String, duration: Int = Toast.LENGTH_SHORT) {
    currentToast?.cancel()
    currentToast = Toast.makeText(context, message, duration).apply {
        show()
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PikabuTrainingTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    SettingsScreen()
                }
            }
        }
    }
}

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
fun SettingsScreen() {
    val context = LocalContext.current

    Column {
        InternalLinksSection(context)
        ExternalLinksSection(context)
    }
}

@Composable
fun InternalLinksSection(context: Context) {
    Column(
        modifier = Modifier.padding(bottom = 16.dp)
    ) {
        internalLinks.forEach { link ->
            LinkItem(link) {
                showToast(context, link.text, Toast.LENGTH_SHORT)
            }
            HorizontalDivider()
        }
    }
}

@Composable
fun ExternalLinksSection(context: Context) {
    Column {
        externalLinks.forEach { link ->
            LinkItem(link) {
                showToast(context, link.text, Toast.LENGTH_SHORT)
            }
        }
    }
}

@Composable
fun LinkItem(item: LinkItem, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 8.dp)
            .semantics { contentDescription = getContentDescription(item) },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LeadingIcon(item.iconResource)
        LinkText(item.text)
        Spacer(modifier = Modifier.weight(1f))
        TrailingIcon(item.type)
    }
}

@Composable
fun LeadingIcon(iconResource: Int) {
    Icon(
        painter = painterResource(id = iconResource),
        contentDescription = null,
        modifier = Modifier.size(16.dp)
    )
}

@Composable
fun LinkText(text: String) {
    Text(
        text = text, modifier = Modifier
            .padding(8.dp)
    )
}

@Composable
fun TrailingIcon(type: LinkType) {
    val iconResource = getIconResource(type)
    Icon(
        painter = painterResource(id = iconResource),
        contentDescription = null,
        modifier = Modifier.size(24.dp)
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
fun GreetingPreview() {
    PikabuTrainingTheme {
        SettingsScreen()
    }
}