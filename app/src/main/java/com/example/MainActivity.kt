package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.example.ui.admin.AdminLoginScreen
import com.example.ui.admin.AdminPanelScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.user.NotificationsScreen
import com.example.ui.user.UserMainScreen
import com.example.viewmodel.AppMode
import com.example.viewmodel.HorrorViewModel
import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext

class MainActivity : ComponentActivity() {
    private val viewModel: HorrorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    HorrorAppRoot(viewModel)
                }
            }
        }
    }
}

@Composable
fun HorrorAppRoot(viewModel: HorrorViewModel) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = {}
    )
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
    
    val appMode by viewModel.appMode.collectAsState()

    when (appMode) {
        AppMode.USER -> {
            UserMainScreen(
                viewModel = viewModel,
                onOpenAdminLogin = {
                    viewModel.setAppMode(AppMode.ADMIN_LOGIN)
                }
            )
        }
        AppMode.ADMIN_LOGIN -> {
            AdminLoginScreen(
                viewModel = viewModel,
                onBack = {
                    viewModel.setAppMode(AppMode.USER)
                }
            )
        }
        AppMode.ADMIN_PANEL -> {
            AdminPanelScreen(
                viewModel = viewModel,
                onExitAdmin = {
                    viewModel.setAppMode(AppMode.USER)
                }
            )
        }
        AppMode.NOTIFICATIONS -> {
            NotificationsScreen(
                viewModel = viewModel,
                onBack = {
                    viewModel.setAppMode(AppMode.USER)
                }
            )
        }
    }
}
