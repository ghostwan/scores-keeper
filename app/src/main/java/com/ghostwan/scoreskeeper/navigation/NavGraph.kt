package com.ghostwan.scoreskeeper.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType.Companion.IntType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.ghostwan.scoreskeeper.presentation.games.GamesScreen
import com.ghostwan.scoreskeeper.presentation.update_game.UpdateGameScreen

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
            GamesScreen(
                navigateToUpdateGameScreen = { bookId ->
                    navController.navigate("${Screen.UpdateGameScreen.route}/${bookId}")
                }
            )
        }
        val argumentGameID = "game_id"
        composable(
            route = "${Screen.UpdateGameScreen.route}/{$argumentGameID}",
            arguments = listOf(
                navArgument(argumentGameID) {
                    type = IntType
                }
            )
        ) {
            val gameId = it.arguments?.getInt(argumentGameID) ?: 0
            UpdateGameScreen(
                gameId = gameId,
                navigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}