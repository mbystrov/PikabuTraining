package ru.training.pikabu

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.training.pikabu.pages.PostsPage
import ru.training.pikabu.pages.LinksPage

@Composable
fun MainScreen(
    postViewModel: PostsViewModel,
    linksViewModel: LinksViewModel,
    currentScreenIndex: Int,
    onScreenChange: (Int) -> Unit
) {

    val navItemList = listOf(
        NavItem(Icons.Default.Home),
        NavItem(Icons.Default.MoreVert),
    )

    var selectedIndex by remember { mutableIntStateOf(currentScreenIndex) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                modifier = Modifier.height(32.dp),
            ) {
                navItemList.forEachIndexed { index, navItem ->
                    NavigationBarItem(
                        selected = selectedIndex == index,
                        onClick = {
                            selectedIndex = index
                            onScreenChange(index)
                        },
                        icon = {
                            Icon(
                                imageVector = navItem.icon,
                                contentDescription = "Icon"
                            )
                        },
                    )
                }
            }
        }
    ) { innerPadding ->
        ContentScreen(
            modifier = Modifier.padding(innerPadding),
            selectedIndex = selectedIndex,
            postsViewModel = postViewModel,
            linksViewModel = linksViewModel
        )
    }
}

@Composable
fun ContentScreen(
    modifier: Modifier = Modifier,
    selectedIndex: Int,
    postsViewModel: PostsViewModel,
    linksViewModel: LinksViewModel
) {
    Box(modifier = modifier) {
        when (selectedIndex) {
            0 -> PostsPage(viewModel = postsViewModel)
            1 -> LinksPage(viewModel = linksViewModel)
        }
    }
}