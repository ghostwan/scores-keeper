package com.scoreskeeper

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.scoreskeeper.presentation.navigation.ScoresKeeperNavGraph
import com.scoreskeeper.presentation.theme.ScoresKeeperTheme
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
