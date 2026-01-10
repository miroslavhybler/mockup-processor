@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.app.R


/**
 * @author Miroslav Hýbler <br>
 * created on 04.10.2023
 */
@Composable
fun DetailAppBar(
    title: String,
    navHostController: NavHostController,
    onFavoriteButton: () -> Unit
) {

    TopAppBar(
        title = {
            Text(
                text = title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_back),
                contentDescription = null,
                modifier = Modifier
                    .size(size = 48.dp)
                    .clip(shape = CircleShape)
                    .clickable(onClick = {
                        val startRoute = navHostController.graph.startDestinationRoute
                        val actualRoute = navHostController.currentBackStackEntry
                            ?.destination?.route
                        if (actualRoute != startRoute) {
                            navHostController.popBackStack()
                        }
                    })
                    .padding(all = 10.dp)
            )
        },
        actions = {
            Icon(
                painter = painterResource(id = R.drawable.ic_favorite),
                contentDescription = null,
                modifier = Modifier
                    .size(size = 48.dp)
                    .clip(shape = CircleShape)
                    .clickable(onClick = onFavoriteButton)
                    .padding(all = 10.dp)
            )
        }
    )
}