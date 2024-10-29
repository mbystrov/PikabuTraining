package ru.training.pikabu

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import ru.training.pikabu.ui.theme.PikabuTrainingTheme

private var currentToast: Toast? = null

fun showToast(context: Context, message: String, duration: Int = Toast.LENGTH_SHORT) {
    currentToast?.cancel()
    currentToast = Toast.makeText(context, message, duration).apply {
        show()
    }
}

class MainActivity : ComponentActivity() {
    private val postsViewModel: PostsViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PikabuTrainingTheme {
                MainScreen(postsViewModel, settingsViewModel)
            }
        }
    }
}

