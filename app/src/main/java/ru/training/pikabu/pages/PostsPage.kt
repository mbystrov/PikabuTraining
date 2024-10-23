package ru.training.pikabu.pages

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PostsPage(modifier: Modifier = Modifier) {

    val postsList = generatePosts()

    Column {
        Header("Посты")
        PostsList(posts = postsList)
    }
}

@Composable
fun PostsList(posts: List<Post>) {
    LazyColumn {
        items(posts, key = { post -> post.name}) { post ->
            PostView(name = post.name)
        }
    }
}

@Composable
fun PostView(name: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = CenterVertically
        ) {
            PostIcon(name = name)
            Spacer(modifier = Modifier.width(16.dp))
            PostTitle(name = name)
        }
    }
}

@Composable
fun PostIcon(name: String) {
    Icon(
        imageVector = Icons.Default.Edit,
        contentDescription = "Icon of the post $name",
        modifier = Modifier
            .size(60.dp)
            .clip(CircleShape)
    )
}

@Composable
fun PostTitle(name: String) {
    Text(
        text = name,
        fontSize = 20.sp
    )
}

data class Post(
    val id: String,
    val name: String,
)

fun generatePosts(): List<Post> {
    return List(50) { index ->
        Post(
            id = "$index",
            name = "Пост #${index}",
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PostsPagePreview() {
    PostsPage()
}