package com.ghostwan.scoreskeeper

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.ghostwan.scoreskeeper.presentation.navigation.ScoresKeeperNavGraph
import com.ghostwan.scoreskeeper.presentation.theme.ScoresKeeperTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ScoresKeeperTheme {
                ScoresKeeperNavGraph()
            }
        }
    }
}
