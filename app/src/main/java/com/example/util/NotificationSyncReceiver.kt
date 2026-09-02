package com.example.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.data.HorrorRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationSyncReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        android.util.Log.d("NotificationSyncReceiver", "Received action: $action")

        // 1. Reschedule WorkManager
        try {
            val workManager = WorkManager.getInstance(context)
            val request = OneTimeWorkRequestBuilder<NotificationWorker>().build()
            workManager.enqueueUniqueWork("BootNotificationSync", ExistingWorkPolicy.REPLACE, request)
        } catch (e: Exception) {
            android.util.Log.e("NotificationSyncReceiver", "WorkManager enqueue failed: ${e.message}")
        }

        // 2. Direct Sync in background coroutine with WakeLock
        val pendingResult = goAsync()
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? android.os.PowerManager
        val wakeLock = powerManager?.newWakeLock(
            android.os.PowerManager.PARTIAL_WAKE_LOCK,
            "HorrorHouse::NotificationSyncWakeLock"
        )
        wakeLock?.acquire(30_000L) // 30 seconds max wake lock

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val repository = HorrorRepository(context.applicationContext)
                val notifications = repository.getAllNotifications()

                for (notification in notifications) {
                    if (!NotificationHelper.isNotificationShown(context.applicationContext, notification.id)) {
                        NotificationHelper.showSystemNotification(
                            context = context.applicationContext,
                            notificationId = notification.id.hashCode(),
                            title = notification.title,
                            message = notification.message,
                            imageUrl = notification.imageUrl
                        )
                        NotificationHelper.markNotificationAsShown(context.applicationContext, notification.id)
                    }
                }

                // Check and notify admin of new user submissions in background
                val prefs = context.applicationContext.getSharedPreferences("horror_admin_prefs", Context.MODE_PRIVATE)
                val isUserAdmin = prefs.getBoolean("is_admin", false)
                if (isUserAdmin) {
                    val submissions = repository.getAllUserSubmissionsAdmin()
                    for (sub in submissions) {
                        if (sub.status == "PENDING" && !NotificationHelper.isSubmissionNotified(context.applicationContext, sub.id)) {
                            NotificationHelper.showSystemNotification(
                                context = context.applicationContext,
                                notificationId = sub.id.hashCode(),
                                title = "📥 روایت جدید ثبت شد!",
                                message = "روایتی با عنوان «${sub.title}» توسط ${sub.author_name} ارسال شد و منتظر تایید شماست."
                            )
                            NotificationHelper.markSubmissionNotified(context.applicationContext, sub.id)
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("NotificationSyncReceiver", "Direct sync failed: ${e.message}")
            } finally {
                try {
                    if (wakeLock?.isHeld == true) {
                        wakeLock.release()
                    }
                } catch (_: Exception) {}
                pendingResult.finish()
            }
        }

        // 3. Ensure alarm is scheduled for next check
        scheduleNextAlarm(context)
    }

    companion object {
        const val ACTION_ALARM_SYNC = "com.example.ACTION_SYNC_NOTIFICATIONS"
        private const val REQUEST_CODE = 88421

        fun scheduleNextAlarm(context: Context) {
            try {
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
                val intent = Intent(context, NotificationSyncReceiver::class.java).apply {
                    action = ACTION_ALARM_SYNC
                }
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    REQUEST_CODE,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val intervalMillis = 15 * 60 * 1000L // 15 minutes (battery friendly)
                val triggerAtMillis = System.currentTimeMillis() + intervalMillis

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setInexactRepeating(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        intervalMillis,
                        pendingIntent
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("NotificationSyncReceiver", "Schedule alarm failed: ${e.message}")
            }
        }
    }
}
