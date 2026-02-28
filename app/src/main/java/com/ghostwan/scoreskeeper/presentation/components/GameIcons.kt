package com.ghostwan.scoreskeeper.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Available icons for game customization.
 * Each entry maps a string key (stored in DB) to a Material icon.
 */
object GameIcons {

    val entries: List<Pair<String, ImageVector>> = listOf(
        "SportsEsports" to Icons.Default.SportsEsports,
        "Casino" to Icons.Default.Casino,
        "Extension" to Icons.Default.Extension,
        "Psychology" to Icons.Default.Psychology,
        "Favorite" to Icons.Default.Favorite,
        "Star" to Icons.Default.Star,
        "EmojiEvents" to Icons.Default.EmojiEvents,
        "Whatshot" to Icons.Default.Whatshot,
        "Bolt" to Icons.Default.Bolt,
        "Rocket" to Icons.Default.Rocket,
        "Diamond" to Icons.Default.Diamond,
        "Pets" to Icons.Default.Pets,
        "MusicNote" to Icons.Default.MusicNote,
        "Palette" to Icons.Default.Palette,
        "SportsSoccer" to Icons.Default.SportsSoccer,
        "FitnessCenter" to Icons.Default.FitnessCenter,
        "LocalFireDepartment" to Icons.Default.LocalFireDepartment,
        "Visibility" to Icons.Default.Visibility,
        "FlashOn" to Icons.Default.FlashOn,
        "Anchor" to Icons.Default.Anchor,
        "Leaderboard" to Icons.Default.Leaderboard,
    )

    fun getIcon(name: String): ImageVector =
        entries.firstOrNull { it.first == name }?.second ?: Icons.Default.SportsEsports
}
