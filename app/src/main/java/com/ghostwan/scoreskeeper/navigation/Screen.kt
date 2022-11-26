package com.ghostwan.scoreskeeper.navigation


sealed class Screen(val route: String) {
    object GamesScreen: Screen("games_screen")
    object PartiesScreen: Screen("parties_screen")
}