package com.triwahyu.miwatchu.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.triwahyu.miwatchu.feature.games.ReactionGame
import com.triwahyu.miwatchu.ui.theme.*

@Composable
fun GamesScreen() {
    val gameState by ReactionGame.gameState.collectAsState()
    val lastScore by ReactionGame.lastScore.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🎮 Game Interaktif Jam", style = MaterialTheme.typography.titleMedium, color = NeonCyan)
        Text("Jam tangan sebagai sensor getar & kontroler game", color = TextSecondary, style = MaterialTheme.typography.bodySmall)

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier.fillMaxWidth().height(180.dp),
            colors = CardDefaults.cardColors(containerColor = CyberSurface)
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(gameState, style = MaterialTheme.typography.titleMedium, color = NeonAmber)
                    if (lastScore != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("${lastScore} ms", style = MaterialTheme.typography.displayMedium, color = NeonGreen)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { ReactionGame.startRound() },
            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("▶ Mulai Ronde Baru", color = Color.Black, style = MaterialTheme.typography.titleMedium)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { ReactionGame.onUserReacted() },
            colors = ButtonDefaults.buttonColors(containerColor = NeonAmber),
            modifier = Modifier.fillMaxWidth().height(64.dp)
        ) {
            Text("⚡ TEKAN! (Atau Sentuh Layar Jam)", color = Color.Black, style = MaterialTheme.typography.titleMedium)
        }
    }
}
