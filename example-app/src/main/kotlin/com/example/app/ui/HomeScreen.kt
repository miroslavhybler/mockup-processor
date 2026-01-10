package com.example.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.app.R
import com.example.app.ui.article.ArticlesScreen
import com.example.app.ui.author.AuthorsScreen


/**
 * @author Miroslav Hýbler <br>
 * created on 23.09.2023
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navHostController: NavHostController,
) {
    var selectedIndex by rememberSaveable { mutableIntStateOf(value = 0) }

    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        Box(modifier = Modifier.weight(weight = 1f)) {

            when (selectedIndex) {
                0 -> {
                    ArticlesScreen(
                        navHostController = navHostController,
                    )
                }

                1 -> {
                    AuthorsScreen(
                        navHostController = navHostController,
                    )
                }
            }
        }

        NavigationBar(
            modifier = Modifier.navigationBarsPadding(),
        ) {
            NavigationBarItem(
                selected = selectedIndex == 0,
                onClick = {
                    if (selectedIndex != 0) {
                        selectedIndex = 0
                    }
                },
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_article),
                        contentDescription = null
                    )
                },
                label = {
                    Text(text = "Articles")
                }
            )

            NavigationBarItem(
                selected = selectedIndex == 1,
                onClick = {
                    if (selectedIndex != 1) {
                        selectedIndex = 1
                    }
                },
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_users),
                        contentDescription = null
                    )
                },
                label = {
                    Text(text = "Authors")
                }
            )
        }
    }
}