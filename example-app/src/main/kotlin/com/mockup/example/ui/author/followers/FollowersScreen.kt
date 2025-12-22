@file:OptIn(ExperimentalMaterial3Api::class)

package com.mockup.example.ui.author.followers

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.mockup.core.Mockup
import com.mockup.example.R
import com.mockup.example.Reader
import com.mockup.example.ui.DetailAppBar
import com.mockup.example.ui.Photo
import com.mockup.example.ui.author.AuthorRank
import kotlinx.coroutines.launch


/**
 * @author Miroslav Hýbler <br>
 * created on 22.12.2025
 */
@Composable
fun FollowersScreen(
    authorId: Int,
    navHostController: NavHostController,
) {

    FollowersScreenContent(
        navHostController = navHostController,
        followers = Mockup.fromJson(
            json = """
[
{
"userName": {
"surname": "John",
"birthname": "Doe"
}
},
{
"userName": {
"surname": "Jane",
"birthname": "Doe"
}
}
]
        """.trimIndent()
        )
    )
}


@Composable
private fun FollowersScreenContent(
    followers: List<Reader>,
    navHostController: NavHostController,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Followers") },
                navigationIcon = {
                    IconButton(
                        onClick = { navHostController.navigateUp() }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_back),
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        content = { paddingValues ->
            LazyColumn(
                content = {
                    items(
                        items = followers,
                    ) {
                        Text(
                            modifier = Modifier
                                .padding(horizontal = 18.dp, vertical = 4.dp)
                                .fillMaxWidth(),
                            text = "${it.surname} ${it.birthname}"
                        )
                    }
                },
                contentPadding = paddingValues,
                modifier = Modifier.fillMaxSize()
            )
        },
    )
}


@Composable
@PreviewLightDark
private fun FollowersScreenPreview() {
    FollowersScreenContent(
        navHostController = rememberNavController(),
        followers = Mockup.fromJson(
            json = """
[
{
"userName": {
"surname": "John",
"birthname": "Doe"
}
},
{
"userName": {
"surname": "Jane",
"birthname": "Doe"
}
}
]
        """.trimIndent()
        )
    )
}