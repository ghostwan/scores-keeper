package com.scoreskeeper.presentation.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object CreateGame : Screen("create_game")
    data object GameDetail : Screen("game/{gameId}") {
        fun createRoute(gameId: Long) = "game/$gameId"
    }
    data object Session : Screen("session/{sessionId}") {
        fun createRoute(sessionId: Long) = "session/$sessionId"
    }
}
