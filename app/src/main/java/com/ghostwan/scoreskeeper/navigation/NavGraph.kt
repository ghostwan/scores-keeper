package com.ghostwan.scoreskeeper.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType.Companion.StringType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.ghostwan.scoreskeeper.presentation.games.GamesScreen
import com.ghostwan.scoreskeeper.presentation.parties.PartiesScreen

@Composable
fun NavGraph (
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = Screen.GamesScreen.route
    ) {
        composable(
            route = Screen.GamesScreen.route
        ) {
            GamesScreen()
        }
        val gameName = "game_name"
        composable(
            route = "${Screen.PartiesScreen.route}/{$gameName}",
            arguments = listOf(
                navArgument(gameName) {
                    type = StringType
                }
            )
        ) {
            PartiesScreen(
                gameId = it.arguments?.getString(gameName) ?: ""
            ) {
                navController.popBackStack()
            }
        }
    }
}