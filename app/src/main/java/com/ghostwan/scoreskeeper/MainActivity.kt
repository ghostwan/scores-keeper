package com.ghostwan.scoreskeeper

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.ghostwan.scoreskeeper.navigation.NavGraph
import com.ghostwan.scoreskeeper.ui.theme.ScoresKeeperTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ScoresKeeperTheme {
                NavGraph(
                    navController = rememberNavController()
                )
            }
        }
    }
}
