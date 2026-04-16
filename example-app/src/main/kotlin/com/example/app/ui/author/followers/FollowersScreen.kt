@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.app.ui.author.followers

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.mockup.core.Mockup
import com.example.app.R
import com.mockup.exampledata.Reader


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