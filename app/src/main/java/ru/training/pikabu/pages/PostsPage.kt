package ru.training.pikabu.pages

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import ru.training.pikabu.ui.theme.PikabuTrainingTheme

@Composable
fun PostsPage(modifier: Modifier = Modifier) {
    Column {
        Header("Посты")
        Text(
            text = "Posts page",
            fontSize = 40.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PostsPagePreview() {
    PostsPage()
}