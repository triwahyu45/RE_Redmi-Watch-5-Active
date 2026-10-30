package com.triwahyu.miwatchu.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.triwahyu.miwatchu.ui.theme.*

@Composable
fun InteractiveScreen() {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("📢 Running Text", "🎮 Game Jam")

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = CyberSurface,
            contentColor = NeonCyan
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title, color = if (selectedTab == index) NeonCyan else TextSecondary) }
                )
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            when (selectedTab) {
                0 -> MarqueeScreen()
                1 -> GamesScreen()
            }
        }
    }
}
