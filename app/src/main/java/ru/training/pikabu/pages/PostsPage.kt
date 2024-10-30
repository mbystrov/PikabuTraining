package ru.training.pikabu.pages

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import ru.training.pikabu.PostsViewModel
import ru.training.pikabu.data.model.Post
import ru.training.pikabu.ui.theme.PikabuDimensions
import ru.training.pikabu.ui.theme.PikabuShapes

@Composable
fun PostsPage(modifier: Modifier = Modifier, viewModel: PostsViewModel) {

    val postsList by viewModel.postsData.collectAsState()

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        Header(text = "Посты")
        PostsList(posts = postsList, modifier = Modifier.weight(1f))
        PostsControlButtons(
            addPostButtonOnClick = { viewModel.createPost() },
            deletePostButtonOnClick = { viewModel.deletePost() },
        )
    }
}

@Composable
fun PostsList(modifier: Modifier = Modifier, posts: List<Post>) {
    LazyColumn(modifier = modifier) {
        items(posts, key = { post -> post.id }) { post ->
            PostView(name = post.name)
        }
    }
}

@Composable
fun PostView(modifier: Modifier = Modifier, name: String) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(PikabuDimensions.paddingMedium),
        shape = PikabuShapes.Shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(PikabuDimensions.paddingLarge),
            verticalAlignment = CenterVertically
        ) {
            PostIcon(name = name)
            Spacer(modifier = Modifier.width(16.dp))
            PostTitle(name = name)
        }
    }
}

@Composable
fun PostIcon(modifier: Modifier = Modifier, name: String) {
    Icon(
        imageVector = Icons.Default.Edit,
        contentDescription = "Icon of the post $name",
        modifier = modifier
            .clip(MaterialTheme.shapes.large)
            .size(PikabuDimensions.iconSizeExtraLarge)
    )
}

@Composable
fun PostTitle(modifier: Modifier = Modifier, name: String) {
    Text(
        modifier = modifier,
        text = name,
        style = MaterialTheme.typography.titleLarge
    )
}

@Composable
fun PostsControlButtons(
    addPostButtonOnClick: () -> Unit,
    deletePostButtonOnClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.fillMaxWidth()) {
        AddPostButton(onClick = addPostButtonOnClick, modifier = Modifier.weight(1f))
        DeletePostButton(onClick = deletePostButtonOnClick, modifier = Modifier.weight(1f))
    }
}

@Composable
fun AddPostButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(PikabuDimensions.paddingMedium),
    ) {
        Text(text = "Добавить пост")
    }
}

@Composable
fun DeletePostButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(PikabuDimensions.paddingMedium),
    ) {
        Text(text = "Удалить пост")
    }
}

//@Preview(showBackground = true)
//@Composable
//fun PostsPagePreview() {
//    PostsPage()
//}
