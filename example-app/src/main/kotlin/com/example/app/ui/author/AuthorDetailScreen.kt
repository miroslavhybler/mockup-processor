@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.app.ui.author

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.mockup.article
import com.mockup.core.Mockup
import com.example.app.Article
import com.example.app.AuthorRank
import com.example.app.ExampleTheme
import com.example.app.Publisher
import com.example.app.PublisherMockupProvider
import com.example.app.ui.DetailAppBar
import com.example.app.ui.Photo
import com.mockup.publisher
import kotlinx.coroutines.launch

/**
 * @author Miroslav Hýbler <br>
 * created on 23.09.2023
 */
@Composable
fun AuthorDetailScreen(
    navHostController: NavHostController,
    authorId: Int
) {

    AuthorDetailScreenContent(
        author = remember { Mockup.publisher.list.find { it.id == authorId }!! },
        navHostController = navHostController,
        articles = remember { Mockup.article.list.take(n = 5) }
    )

}


@Composable
private fun AuthorDetailScreenContent(
    modifier: Modifier = Modifier,
    author: Publisher,
    articles: List<Article>,
    navHostController: NavHostController
) {

    val snackBarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()


    Scaffold(
        modifier = modifier,
        topBar = {
            DetailAppBar(
                title = author.fullName,
                navHostController = navHostController,
                onFavoriteButton = {
                    coroutineScope.launch {
                        snackBarHostState.showSnackbar(
                            message = "Saved to favorites \uD83D\uDE0A",
                            actionLabel = "Dismiss",
                            duration = SnackbarDuration.Short
                        )
                    }
                }
            )
        },
        content = { paddingValues ->
            LazyColumn(
                content = {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth(),
                        ) {

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(height = 256.dp)
                            ) {
                                Photo(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(height = 226.dp),
                                    imageUrl = author.themeImageUrl,
                                )
                                Photo(
                                    modifier = Modifier
                                        .align(alignment = Alignment.BottomStart)
                                        .padding(start = 32.dp, top = 22.dp)
                                        .size(size = 96.dp)
                                        .clip(shape = CircleShape),
                                    imageUrl = author.avatarUrl,

                                    )

                                AuthorRank(
                                    modifier = Modifier
                                        .padding(top = 16.dp, end = 16.dp)
                                        .align(alignment = Alignment.TopEnd),
                                    authorRank = author.authorRank,
                                )
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(space = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(space = 4.dp)
                                ) {
                                    Text(
                                        text = author.fullName,
                                        style = MaterialTheme.typography.headlineSmall,
                                        color = MaterialTheme.colorScheme.onBackground,
                                        modifier = Modifier.padding(horizontal = 12.dp),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Text(
                                        text = "${author.articlesCount} articles written 📝",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onBackground,
                                        modifier = Modifier.padding(horizontal = 12.dp),
                                        maxLines = 10
                                    )
                                }

                                Button(
                                    onClick = {
                                        navHostController.navigate(route = "author/${author.id}/followers")
                                    }
                                ) {
                                    Text(text = "Followers")
                                }
                            }
                            Spacer(modifier = Modifier.height(height = 4.dp))

                            Text(
                                text = author.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(horizontal = 12.dp),
                                maxLines = 10
                            )
                        }

                        Spacer(modifier = Modifier.height(height = 32.dp))

                        Text(
                            text = "Most popular",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(horizontal = 18.dp)
                        )

                        Spacer(modifier = Modifier.height(height = 8.dp))

                    }

                    items(items = articles) { article ->
                        ArticleItem(
                            article = article,
                            onClick = {
                                navHostController.navigate(route = "article/${article.id}")
                            }
                        )
                    }
                },
                contentPadding = paddingValues,
                modifier = Modifier.fillMaxSize()
            )
        },
        snackbarHost = {
            SnackbarHost(snackBarHostState) { snackBarData ->
                Snackbar(snackbarData = snackBarData)
            }
        }
    )
}


@Composable
fun AuthorRank(
    modifier: Modifier = Modifier,
    authorRank: AuthorRank,
    isBig: Boolean = true,
) {
    val containerColor = when (authorRank) {
        AuthorRank.GOLD -> Color(color = 0xFFFFC107)
        AuthorRank.SILVER -> Color(color = 0xFF797979)
        AuthorRank.BRONZE -> Color(color = 0xFF724400)
    }


    Box(
        modifier = modifier
            .background(
                color = containerColor,
                shape = RoundedCornerShape(size = 16.dp),
            )
            .padding(
                horizontal = if (isBig) 12.dp else 4.dp,
                vertical = if (isBig) 8.dp else 4.dp
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = authorRank.name,
            style = if (isBig)
                MaterialTheme.typography.titleMedium
            else
                MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
        )
    }
}


@Composable
private fun ArticleItem(
    modifier: Modifier = Modifier,
    article: Article,
    onClick: () -> Unit
) {
    Row(
        modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Photo(
            imageUrl = article.imageUrl,
            modifier = Modifier
                .size(size = 64.dp)
                .clip(shape = RoundedCornerShape(size = 16.dp))
        )

        Spacer(modifier = Modifier.width(width = 16.dp))
        Column(modifier = Modifier.weight(weight = 1f)) {
            Text(
                text = article.title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleSmall
            )
            Spacer(modifier = Modifier.height(height = 2.dp))

            Text(
                text = article.content,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodySmall
            )
        }

    }
}


@Composable
@PreviewLightDark
private fun AuthorDetailScreenPreview(
    @PreviewParameter(provider = PublisherMockupProvider::class)
    publisher: Publisher,
) {
    ExampleTheme {
        AuthorDetailScreenContent(
            author = publisher,
            navHostController = rememberNavController(),
            articles = Mockup.getList(),
        )
    }
}