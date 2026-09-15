package com.example.ads

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.adivery.sdk.Adivery
import com.adivery.sdk.AdiveryAdListener
import com.adivery.sdk.AdiveryBannerAdView
import com.adivery.sdk.AdiveryListener
import com.adivery.sdk.BannerSize

object AdiveryAdManager {
    private const val TAG = "AdiveryAdManager"

    const val APP_ID = "6104a59b-ca45-4f84-9dc6-bd5d9a577b22"
    const val BANNER_PLACEMENT_ID = "dbc8dbe2-6189-4bbf-b956-a5f1809f9832"
    const val APP_OPEN_PLACEMENT_ID = "dd82b3f2-2694-43e8-bf3e-7d6eb923aea1"

    private var isInitialized = false

    fun initialize(activity: Activity) {
        if (isInitialized) return
        try {
            Adivery.configure(activity.application, APP_ID)
            Adivery.prepareAppOpenAd(activity, APP_OPEN_PLACEMENT_ID)
            isInitialized = true
            Log.d(TAG, "Adivery initialized successfully with App ID: $APP_ID")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Adivery", e)
        }
    }

    fun prepareAppOpenAd(activity: Activity) {
        try {
            Adivery.prepareAppOpenAd(activity, APP_OPEN_PLACEMENT_ID)
        } catch (e: Exception) {
            Log.e(TAG, "Error preparing App Open ad", e)
        }
    }

    fun isAppOpenLoaded(): Boolean {
        return try {
            Adivery.isLoaded(APP_OPEN_PLACEMENT_ID)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Shows the App Open ad if available.
     * If the ad is not available, or in case of error/timeout, invokes [onComplete]
     * so the user can continue reading without being blocked.
     * If [isPremium] is true, [onComplete] is called immediately without showing any ad.
     */
    fun showAppOpenAdIfAvailable(
        activity: Activity,
        isPremium: Boolean,
        onComplete: () -> Unit
    ) {
        if (isPremium) {
            onComplete()
            return
        }

        if (!isAppOpenLoaded()) {
            Log.d(TAG, "App Open ad not loaded, proceeding directly.")
            prepareAppOpenAd(activity)
            onComplete()
            return
        }

        var completed = false
        val mainHandler = Handler(Looper.getMainLooper())

        val finishAction = Runnable {
            if (!completed) {
                completed = true
                prepareAppOpenAd(activity)
                onComplete()
            }
        }

        val listener = object : AdiveryListener() {}

        try {
            Adivery.addListener(listener)
            Adivery.showAd(APP_OPEN_PLACEMENT_ID)
            // 7 seconds fallback timeout to guarantee user is never blocked
            mainHandler.postDelayed(finishAction, 7000)
        } catch (e: Exception) {
            Log.e(TAG, "Error invoking Adivery.showAd", e)
            finishAction.run()
        }
    }
}

/**
 * Standard Banner Ad Composable placed at the bottom center of screens.
 * When [isPremium] is true, it renders nothing.
 */
@Composable
fun AdiveryBottomBannerAd(
    isPremium: Boolean,
    modifier: Modifier = Modifier
) {
    if (isPremium) return

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF030106))
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            factory = { context ->
                AdiveryBannerAdView(context).apply {
                    setBannerSize(BannerSize.BANNER)
                    setPlacementId(AdiveryAdManager.BANNER_PLACEMENT_ID)
                    setBannerAdListener(object : AdiveryAdListener() {
                        override fun onAdLoaded() {
                            Log.d("AdiveryBanner", "Banner loaded successfully")
                        }

                        override fun onError(reason: String?) {
                            Log.w("AdiveryBanner", "Banner load failed: $reason")
                        }
                    })
                    loadAd()
                }
            }
        )
    }
}
