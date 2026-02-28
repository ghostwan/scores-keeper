package com.ghostwan.scoreskeeper.presentation.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object CreateGame : Screen("create_game")
    data object Settings : Screen("settings")
    data object GameDetail : Screen("game/{gameId}") {
        fun createRoute(gameId: Long) = "game/$gameId"
    }
    data object Session : Screen("session/{sessionId}") {
        fun createRoute(sessionId: Long) = "session/$sessionId"
    }
}
