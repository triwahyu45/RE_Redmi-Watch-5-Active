package com.triwahyu.miwatchu

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
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
    object Studio : Screen("studio", "Watchface", Icons.Default.Face)
    object Marquee : Screen("marquee", "Running Text", Icons.Default.PlayArrow)
    object Games : Screen("games", "Games", Icons.Default.Star)
    object Toggles : Screen("toggles", "Fitur", Icons.Default.Settings)
    object Status : Screen("status", "Status", Icons.Default.Info)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Cek izin Notification Listener untuk forwarder WA & Call
        if (!isNotificationServiceEnabled()) {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }

        setContent {
            MiWatchUTheme {
                MainApp()
            }
        }
    }

    private fun isNotificationServiceEnabled(): Boolean {
        val flat = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        return flat?.contains(packageName) == true
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp() {
    var selectedScreen by remember { mutableStateOf<Screen>(Screen.Studio) }
    val screens = listOf(
        Screen.Studio,
        Screen.Marquee,
        Screen.Games,
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
                        label = { Text(screen.title) }
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
                is Screen.Marquee -> MarqueeScreen()
                is Screen.Games -> GamesScreen()
                is Screen.Toggles -> TogglesScreen()
                is Screen.Status -> StatusScreen()
            }
        }
    }
}
