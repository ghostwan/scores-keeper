package com.scoreskeeper.presentation.navigation

import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.scoreskeeper.presentation.screens.game.CreateGameScreen
import com.scoreskeeper.presentation.screens.game.GameDetailScreen
import com.scoreskeeper.presentation.screens.home.HomeScreen
import com.scoreskeeper.presentation.screens.session.SessionScreen
import com.scoreskeeper.presentation.screens.settings.SettingsScreen

@Composable
fun ScoresKeeperNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = Modifier.navigationBarsPadding(),
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToCreateGame = { navController.navigate(Screen.CreateGame.route) },
                onNavigateToGame = { gameId ->
                    navController.navigate(Screen.GameDetail.createRoute(gameId))
                },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }

        composable(Screen.CreateGame.route) {
            CreateGameScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }

        composable(
            route = Screen.GameDetail.route,
            arguments = listOf(navArgument("gameId") { type = NavType.LongType }),
        ) {
            GameDetailScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToSession = { sessionId ->
                    navController.navigate(Screen.Session.createRoute(sessionId))
                },
            )
        }

        composable(
            route = Screen.Session.route,
            arguments = listOf(navArgument("sessionId") { type = NavType.LongType }),
        ) {
            SessionScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }
    }
}
