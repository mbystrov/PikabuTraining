package ru.training.pikabu.pages

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ru.training.pikabu.PostsViewModel
import ru.training.pikabu.data.model.Post
import ru.training.pikabu.ui.theme.PikabuDimensions
import ru.training.pikabu.ui.theme.PikabuShapes

@Composable
fun PostsPage(modifier: Modifier = Modifier, viewModel: PostsViewModel) {

    val postsList by viewModel.postsData.collectAsState()
    val tagList by viewModel.tagsData.collectAsState()
    val selectedTags by viewModel.selectedTags.collectAsState()

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        Header(text = "Посты")
        TagsGrid(
            tags = tagList.map { it.tagValue },
            selectedTags = selectedTags,
            onTagClick = { tagValue -> viewModel.toggleTag(tagValue) },
            onButtonClick = { tagValue -> viewModel.createTag(tagValue) },
            onDelete = { tagValue -> viewModel.deleteTag(tagValue) }
        )
        PostsList(posts = postsList, modifier = Modifier.weight(1f), selectedTags = selectedTags)
        PostsControlButtons(
            addPostButtonOnClick = { viewModel.createPost() },
            deletePostButtonOnClick = { viewModel.deletePost() },
        )
    }
}

@Composable
fun TagsGrid(
    tags: List<String>,
    selectedTags: Set<String>,
    modifier: Modifier = Modifier,
    onTagClick: (String) -> Unit,
    onButtonClick: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    var text by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
    ) {
        Column {
            Row(Modifier.fillMaxWidth()) {
                tags.take(tags.size / 2).forEach { tag ->
                    TagButton(
                        tag = tag,
                        isSelected = tag in selectedTags,
                        onClick = { onTagClick(tag) },
                        onDelete = { onDelete(tag) })
                }
            }
            Row(Modifier.fillMaxWidth()) {
                tags.drop(tags.size / 2).forEach { tag ->
                    TagButton(
                        tag = tag,
                        isSelected = tag in selectedTags,
                        onClick = { onTagClick(tag) },
                        onDelete = { onDelete(tag) })
                }
            }
            Row {
                TextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text(text = "Имя тэга") }
                )
                Button(onClick = { onButtonClick(text) }) {
                    Text(text = "Добавить тэг")
                }
            }
        }
    }
}

@Composable
fun TagButton(
    tag: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Box(modifier = modifier) {
        Surface(
            shape = Shapes().extraSmall,
            color =
            if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(1.dp)
                .clickable(onClick = onClick)
        ) {
            Text(text = tag, modifier = Modifier.padding(end = PikabuDimensions.paddingLarge))
        }
        IconButton(
            onClick = onDelete,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(PikabuDimensions.paddingLarge)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Delete tag",
                tint = if (isSelected) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
fun PostsList(modifier: Modifier = Modifier, posts: List<Post>, selectedTags: Set<String>) {
    LazyColumn(modifier = modifier) {
        items(posts, key = { post -> post.id }) { post ->
            PostView(name = post.name, selectedTags = selectedTags)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PostView(modifier: Modifier = Modifier, name: String, selectedTags: Set<String>) {
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
            Spacer(modifier = Modifier.width(PikabuDimensions.paddingLarge))
            PostTitle(name = name)
        }
        if (selectedTags.isNotEmpty()) {
            FlowRow {
                selectedTags.forEach { tag ->
                    Text(
                        text = tag,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = PikabuDimensions.paddingSmall)
                    )
                }
            }
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
