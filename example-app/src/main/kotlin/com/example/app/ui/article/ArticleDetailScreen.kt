@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.example.app.ui.article

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.mockup.core.Mockup
import com.mockup.exampledata.Article
import com.example.app.ExampleTheme
import com.example.app.ui.DetailAppBar
import com.example.app.ui.Photo
import com.mockup.exampledata.articleMockupProvider
import com.mockup.exampledata.publisherMockupProvider
import kotlinx.coroutines.launch


/**
 * @author Miroslav Hýbler <br>
 * created on 23.09.2023
 */
@Composable
fun ArticleDetailScreen(
    navHostController: NavHostController,
    articleId: Int,
) {

    val article = remember {
        Mockup.articleMockupProvider.list.find { article -> article.id == articleId }!!
    }

    ArticleDetailScreenContent(
        article = article,
        navHostController = navHostController
    )
}

@Composable
private fun ArticleDetailScreenContent(
    article: Article,
    navHostController: NavHostController
) {

    val coroutineScope = rememberCoroutineScope()
    val snackBarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            DetailAppBar(
                title = article.title,
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(state = rememberScrollState())
                    .padding(paddingValues = paddingValues),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(height = 312.dp)
                ) {
                    Photo(
                        imageUrl = article.imageUrl,
                        modifier = Modifier.matchParentSize()
                    )
                }


                Text(
                    text = article.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(horizontal = 12.dp),
                    maxLines = 6,
                    overflow = TextOverflow.Ellipsis
                )

                FlowRow(
                    modifier = Modifier
                        .padding(top = 12.dp, end = 8.dp, start = 8.dp)
                        .align(alignment = Alignment.Start)
                ) {
                    article.categories.forEach { category ->
                        Text(
                            text = category.formattedName,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier
                                .padding(all = 2.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = RoundedCornerShape(size = 8.dp)
                                )
                                .padding(vertical = 4.dp, horizontal = 4.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(height = 4.dp))

                Row(
                    modifier = Modifier
                        .wrapContentWidth()
                        .clip(shape = CircleShape)
                        .clickable(
                            onClick = {
                                //As described in the readme.md data are not aggregated, can't use
                                //Author id from article because he is not in publisher list probably
                                navHostController.navigate(
                                    route = "author/${Mockup.publisherMockupProvider.list.random().id}"
                                )
                            }
                        )
                        .padding(horizontal = 12.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Photo(
                        imageUrl = article.author.avatarUrl,
                        modifier = Modifier
                            .size(size = 32.dp)
                            .clip(shape = CircleShape)
                    )

                    Spacer(modifier = Modifier.width(width = 14.dp))

                    Text(
                        text = article.author.fullName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(height = 12.dp))

                Text(
                    text = remember(key1 = article) { article.createdAtFormatted },
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                Spacer(modifier = Modifier.height(height = 12.dp))

                Text(
                    text = article.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )


                Text(
                    text = "Gallery",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                FlowRow {
                    article.gallery.forEach { galleryPhoto ->
                        Photo(
                            imageUrl = galleryPhoto.imageUrl,
                            modifier = Modifier
                                .padding(vertical = 8.dp, horizontal = 8.dp)
                                .size(size = 96.dp)
                                .clip(shape = RoundedCornerShape(size = 16.dp))
                        )
                    }
                }

            }
        },
        snackbarHost = {
            SnackbarHost(snackBarHostState) { snackBarData ->
                Snackbar(snackbarData = snackBarData)
            }
        }
    )
}


@Composable
@PreviewLightDark
private fun ArticleDetailScreenPreview() {
    ExampleTheme() {
        ArticleDetailScreenContent(
            article = Mockup.get(),
            navHostController = rememberNavController()
        )
    }
}