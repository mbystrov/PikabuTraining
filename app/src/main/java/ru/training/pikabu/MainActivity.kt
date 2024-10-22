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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
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
                    SettingScreen()
                }
            }
        }
    }
}


@Composable
fun SettingScreen() {
    val context = LocalContext.current

    Column {
        Column(
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            InternalLinkItem(text = "Комментарии дня", R.drawable.comment) {
                showToast(context, "Комментарии дня", Toast.LENGTH_SHORT)
            }
            HorizontalDivider()
            InternalLinkItem(text = "О нас", R.drawable.circle_exclamation) {
                showToast(context, "О нас", Toast.LENGTH_SHORT)
            }
            HorizontalDivider()
            InternalLinkItem(text = "Внешний вид", R.drawable.palette) {
                showToast(context, "Внешний вид", Toast.LENGTH_SHORT)
            }
            HorizontalDivider()
        }
        Column {
            ExternalLinkItem(text = "Кодекс Пикабу", R.drawable.pikabu_cake) {
                showToast(context, "Кодекс Пикабу", Toast.LENGTH_SHORT)
            }
            ExternalLinkItem(text = "Правила соцсети", R.drawable.megaphone) {
                showToast(context, "Правила соцсети", Toast.LENGTH_SHORT)
            }
            ExternalLinkItem(text = "О рекомендациях", R.drawable.open_book) {
                showToast(context, "О рекомендациях", Toast.LENGTH_SHORT)
            }
            ExternalLinkItem(text = "FAQ", R.drawable.circle_exclamation) {
                showToast(context, "FAQ", Toast.LENGTH_SHORT)
            }
            ExternalLinkItem(text = "Магазин", R.drawable.shop) {
                showToast(context, "Магазин", Toast.LENGTH_SHORT)
            }
            ExternalLinkItem(text = "Зал славы", R.drawable.prize) {
                showToast(context, "Зал славы", Toast.LENGTH_SHORT)
            }
        }
    }
}

@Composable
fun InternalLinkItem(text: String, iconResource: Int, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 8.dp)
            .semantics { contentDescription = "$text, нажмите для открытия экрана" },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(id = iconResource),
            contentDescription = null,
            modifier = Modifier
                .size(16.dp)
        )
        Text(
            text = text, modifier = Modifier
                .padding(8.dp)
                .weight(1f)
        )
        Image(
            painter = painterResource(id = R.drawable.arrow),
            contentDescription = null,
            modifier = Modifier
                .size(24.dp)
        )
    }
}

@Composable
fun ExternalLinkItem(text: String, iconResource: Int, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 8.dp)
            .semantics { contentDescription = "$text, нажмите для перехода на сайт" },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(id = iconResource),
            contentDescription = null,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = text, modifier = Modifier
                .padding(8.dp)
                .weight(1f)
        )
        Image(
            painter = painterResource(id = R.drawable.new_tab),
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    PikabuTrainingTheme {
        SettingScreen()
    }
}