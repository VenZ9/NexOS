package dev.nexos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dev.nexos.desktop.DesktopScreen
import dev.nexos.desktop.DesktopViewModel
import dev.nexos.ui.theme.NexOSTheme

class MainActivity : ComponentActivity() {

    private val desktopViewModel: DesktopViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val app = NexApplication.instance
                return DesktopViewModel(
                    preferencesRepository = app.preferencesRepository,
                    fileRepository = app.fileRepository,
                    runtimeProvider = app.runtimeProvider
                ) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val prefs by desktopViewModel.preferences.collectAsState()
            val isStartMenuOpen by desktopViewModel.isStartMenuOpen.collectAsState()
            val isFlyoutOpen by desktopViewModel.isSystemTrayFlyoutOpen.collectAsState()
            val windows by desktopViewModel.windows.collectAsState()

            BackHandler(enabled = isStartMenuOpen || isFlyoutOpen || windows.any { it.isFocused }) {
                when {
                    isStartMenuOpen || isFlyoutOpen -> desktopViewModel.dismissAllPopups()
                    else -> {
                        val focused = desktopViewModel.windowManager.getFocusedWindow()
                        if (focused != null) {
                            desktopViewModel.minimizeWindow(focused.id)
                        }
                    }
                }
            }

            NexOSTheme(accentColor = Color(prefs.accentColorHex)) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF18181B)
                ) {
                    DesktopScreen(viewModel = desktopViewModel)
                }
            }
        }
    }
}
