package com.example.util

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.HorrorRepository

class NotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val repository = HorrorRepository(applicationContext)
            val notifications = repository.getAllNotifications()

            for (notification in notifications) {
                if (!NotificationHelper.isNotificationShown(applicationContext, notification.id)) {
                    NotificationHelper.showSystemNotification(
                        context = applicationContext,
                        notificationId = notification.id.hashCode(),
                        title = notification.title,
                        message = notification.message,
                        imageUrl = notification.imageUrl
                    )
                    NotificationHelper.markNotificationAsShown(applicationContext, notification.id)
                }
            }
            Result.success()
        } catch (e: Exception) {
            android.util.Log.e("NotificationWorker", "Background notification sync failed: ${e.message}")
            Result.retry()
        }
    }
}
