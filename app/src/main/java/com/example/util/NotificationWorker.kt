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

            // Check and notify admin of new user submissions in background worker
            val prefs = applicationContext.getSharedPreferences("horror_admin_prefs", Context.MODE_PRIVATE)
            val isUserAdmin = prefs.getBoolean("is_admin", false)
            if (isUserAdmin) {
                val submissions = repository.getAllUserSubmissionsAdmin()
                for (sub in submissions) {
                    if (sub.status == "PENDING" && !NotificationHelper.isSubmissionNotified(applicationContext, sub.id)) {
                        NotificationHelper.showSystemNotification(
                            context = applicationContext,
                            notificationId = sub.id.hashCode(),
                            title = "📥 روایت جدید ثبت شد!",
                            message = "روایتی با عنوان «${sub.title}» توسط ${sub.author_name} ارسال شد و منتظر تایید شماست."
                        )
                        NotificationHelper.markSubmissionNotified(applicationContext, sub.id)
                    }
                }
            }
            Result.success()
        } catch (e: Exception) {
            android.util.Log.e("NotificationWorker", "Background notification sync failed: ${e.message}")
            Result.retry()
        }
    }
}
