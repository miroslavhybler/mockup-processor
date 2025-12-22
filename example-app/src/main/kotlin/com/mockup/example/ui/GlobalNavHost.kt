package com.mockup.example.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.mockup.example.ui.article.ArticleDetailScreen
import com.mockup.example.ui.author.AuthorDetailScreen
import com.mockup.example.ui.author.followers.FollowersScreen


/**
 * @author Miroslav Hýbler <br>
 * created on 23.09.2023
 */
@Composable
fun GlobalNavHost(navHostController: NavHostController) {


    NavHost(navController = navHostController, startDestination = "home") {

        composable(route = "home") {
            HomeScreen(navHostController = navHostController)

        }

        composable(
            route = "article/{id}",
            arguments = listOf(
                navArgument(name = "id") {
                    type = NavType.IntType
                    defaultValue = -1
                    nullable = false
                }
            )
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getInt("id") ?: -1

            ArticleDetailScreen(
                navHostController = navHostController,
                articleId = id
            )
        }

        composable(
            route = "author/{id}",
            arguments = listOf(
                navArgument(name = "id") {
                    type = NavType.IntType
                    defaultValue = -1
                    nullable = false
                }
            )
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getInt("id") ?: -1

            AuthorDetailScreen(
                navHostController = navHostController,
                authorId = id
            )
        }

        composable(
            route = "author/{id}/followers",
            arguments = listOf(
                navArgument(name = "id") {
                    type = NavType.IntType
                    defaultValue = -1
                    nullable = false
                }
            )
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getInt("id") ?: -1
            FollowersScreen(
                authorId = id,
                navHostController = navHostController,
            )
        }
    }

}