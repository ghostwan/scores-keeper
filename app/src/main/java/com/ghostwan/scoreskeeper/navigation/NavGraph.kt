package com.ghostwan.scoreskeeper.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType.Companion.LongType
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
        val gameID = "game_id"
        composable(
            route = "${Screen.PartiesScreen.route}/{$gameID}",
            arguments = listOf(
                navArgument(gameID) {
                    type = LongType
                }
            )
        ) {
            val gameId = it.arguments?.getInt(gameID) ?: 0
            PartiesScreen(
                gameId = gameId
            ) {
                navController.popBackStack()
            }
        }
    }
}