package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import java.net.URL

object NotificationHelper {
    const val CHANNEL_ID = "horror_house_notifications_channel"
    const val CHANNEL_NAME = "اعلان‌های عمارت ارواح"
    const val PREF_SHOWN_NOTIFICATIONS = "horror_house_shown_notifications"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "اعلان‌های مهم، جدیدترین داستان‌ها و اخبار عمارت"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 250, 500)
                enableLights(true)
                lightColor = Color.RED
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showSystemNotification(
        context: Context,
        notificationId: Int,
        title: String,
        message: String,
        imageUrl: String? = null
    ) {
        try {
            // Check POST_NOTIFICATIONS permission on Android 13+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(
                        context,
                        android.Manifest.permission.POST_NOTIFICATIONS
                    ) != android.content.pm.PackageManager.PERMISSION_GRANTED
                ) {
                    android.util.Log.w("NotificationHelper", "POST_NOTIFICATIONS permission not granted")
                    return
                }
            }

            createNotificationChannel(context)

            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                notificationId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("🕯️ $title")
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setColor(Color.parseColor("#B8143F"))
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

            if (!imageUrl.isNullOrBlank()) {
                try {
                    val url = URL(imageUrl)
                    val bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream())
                    if (bitmap != null) {
                        val bigPicStyle = NotificationCompat.BigPictureStyle()
                            .bigPicture(bitmap)
                        builder.setStyle(bigPicStyle)
                    }
                } catch (e: Exception) {
                    android.util.Log.e("NotificationHelper", "Failed to load notification image: ${e.message}")
                }
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(notificationId, builder.build())
        } catch (e: Exception) {
            android.util.Log.e("NotificationHelper", "Error showing system notification: ${e.message}")
        }
    }

    fun markNotificationAsShown(context: Context, notificationId: String) {
        val prefs = context.getSharedPreferences(PREF_SHOWN_NOTIFICATIONS, Context.MODE_PRIVATE)
        prefs.edit().putBoolean("shown_$notificationId", true).apply()
    }

    fun isNotificationShown(context: Context, notificationId: String): Boolean {
        val prefs = context.getSharedPreferences(PREF_SHOWN_NOTIFICATIONS, Context.MODE_PRIVATE)
        return prefs.getBoolean("shown_$notificationId", false)
    }
}
