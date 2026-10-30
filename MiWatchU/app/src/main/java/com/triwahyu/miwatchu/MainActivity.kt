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
    object FlashOS : Screen("flash", "Flash OS", Icons.Default.Build)
    object Studio : Screen("studio", "Watchface", Icons.Default.Face)
    object Interactive : Screen("interactive", "Fitur Jam", Icons.Default.PlayArrow)
    object Toggles : Screen("toggles", "Kontrol", Icons.Default.Settings)
    object Status : Screen("status", "Koneksi", Icons.Default.Info)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Tanpa popup atau redirect otomatis saat app dibuka agar tidak mengganggu aktivitas pengguna
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
    var selectedScreen by remember { mutableStateOf<Screen>(Screen.Studio) }
    val screens = listOf(
        Screen.Studio,
        Screen.FlashOS,
        Screen.Interactive,
        Screen.Toggles,
        Screen.Status
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
                is Screen.Studio -> WatchfaceStudioScreen()
                is Screen.FlashOS -> FlashOSScreen()
                is Screen.Interactive -> InteractiveScreen()
                is Screen.Toggles -> TogglesScreen()
                is Screen.Status -> StatusScreen()
            }
        }
    }
}
