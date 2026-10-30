package com.triwahyu.miwatchu

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.triwahyu.miwatchu.ui.screens.*
import com.triwahyu.miwatchu.ui.theme.*

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object RunningText : Screen("running_text", "Running Text", Icons.Default.PlayArrow)
    object Pairing : Screen("pairing", "Pairing Jam", Icons.Default.Share)
    object WatchApps : Screen("calculator", "Kalkulator", Icons.Default.Edit)
    object Studio : Screen("studio", "Watchface", Icons.Default.Face)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MiWatchUTheme {
                MainApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp() {
    var selectedScreen by remember { mutableStateOf<Screen>(Screen.RunningText) }
    val screens = listOf(
        Screen.RunningText,
        Screen.Pairing,
        Screen.WatchApps,
        Screen.Studio
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi WatchU • ${selectedScreen.title}", color = NeonCyan) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CyberSurface)
            )
        },
        bottomBar = {
            NavigationBar(containerColor = CyberSurface) {
                screens.forEach { screen ->
                    NavigationBarItem(
                        selected = selectedScreen == screen,
                        onClick = { selectedScreen = screen },
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CyberBlack,
                            selectedTextColor = NeonCyan,
                            indicatorColor = NeonCyan,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        )
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(CyberBlack)
                .padding(padding)
        ) {
            when (selectedScreen) {
                is Screen.RunningText -> SyncRunningTextScreen()
                is Screen.Pairing -> PairingScreen()
                is Screen.WatchApps -> WatchAppsScreen()
                is Screen.Studio -> WatchfaceStudioScreen()
            }
        }
    }
}
