package com.ghostwan.scoreskeeper.navigation


sealed class Screen(val route: String) {
    object GamesScreen: Screen("game_screen")
    object UpdateGameScreen: Screen("update_game_screen")
}