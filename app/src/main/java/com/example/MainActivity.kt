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
import com.example.ui.user.AgeAndHealthGateScreen
import com.example.ui.user.UserMainScreen
import com.example.viewmodel.AppMode
import com.example.viewmodel.HorrorViewModel

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
    val appMode by viewModel.appMode.collectAsState()
    val hasConfirmedAgeAndHealth by viewModel.hasConfirmedAgeAndHealth.collectAsState()

    if (!hasConfirmedAgeAndHealth) {
        AgeAndHealthGateScreen(
            onConfirm = {
                viewModel.confirmAgeAndHealth()
            }
        )
    } else {
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
        }
    }
}
