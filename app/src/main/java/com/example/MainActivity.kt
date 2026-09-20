package com.example

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
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
import com.example.ads.AdiveryAdManager
import com.example.billing.MyketBillingManager
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.user.AgeAndHealthGateScreen
import com.example.ui.user.HorrorOnboardingScreen
import com.example.ui.user.UserMainScreen
import com.example.viewmodel.HorrorViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: HorrorViewModel by viewModels()
    private lateinit var billingManager: MyketBillingManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Adivery Ad Network
        AdiveryAdManager.initialize(this)

        // Setup global uncaught crash reporting to Supabase app_crash_logs
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                viewModel.reportCrash(applicationContext, throwable, "Uncaught Exception in ${thread.name}")
                Thread.sleep(800)
            } catch (e: Exception) {
                // Ignore
            }
            defaultHandler?.uncaughtException(thread, throwable)
        }

        // Initialize Myket Billing Manager
        billingManager = MyketBillingManager(this)
        billingManager.startConnection(
            onConnected = {
                // Query owned purchases to restore previous purchases automatically
                billingManager.queryPurchasesAsync { isOwned ->
                    if (isOwned) {
                        viewModel.setPremiumUser(true)
                    }
                }
            }
        )

        setContent {
            MyApplicationTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    HorrorAppRoot(viewModel = viewModel, billingManager = billingManager)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        billingManager.handleActivityResult(
            requestCode = requestCode,
            resultCode = resultCode,
            data = data,
            onSuccess = {
                viewModel.setPremiumUser(true)
                Toast.makeText(this, "عضویت طلایی و دائمی شما با موفقیت فعال شد! 👑", Toast.LENGTH_LONG).show()
            },
            onError = { error ->
                Toast.makeText(this, error, Toast.LENGTH_LONG).show()
            }
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        billingManager.endConnection()
    }
}

@Composable
fun HorrorAppRoot(viewModel: HorrorViewModel, billingManager: MyketBillingManager) {
    val hasConfirmedAgeAndHealth by viewModel.hasConfirmedAgeAndHealth.collectAsState()
    val hasSeenOnboarding by viewModel.hasSeenOnboarding.collectAsState()

    if (!hasConfirmedAgeAndHealth) {
        AgeAndHealthGateScreen(
            onConfirm = {
                viewModel.confirmAgeAndHealth()
            }
        )
    } else if (!hasSeenOnboarding) {
        HorrorOnboardingScreen(
            onFinish = {
                viewModel.completeOnboarding()
            }
        )
    } else {
        UserMainScreen(
            viewModel = viewModel,
            billingManager = billingManager
        )
    }
}
