package com.focusflow.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.focusflow.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ReviewReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                val dueCount = queryDueReviewCount()
                if (dueCount > 0) {
                    sendNotification(dueCount)
                }
                Result.success()
            } catch (e: Exception) {
                Result.failure()
            }
        }
    }

    private suspend fun queryDueReviewCount(): Int {
        // TODO: Query from ReviewLogDao when database is available
        // For now, return 0 as placeholder
        return 0
    }

    private fun sendNotification(dueCount: Int) {
        val context = applicationContext
        createNotificationChannel(context)

        val notification = NotificationCompat.Builder(context, REVIEW_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_review)
            .setContentTitle("复习提醒")
            .setContentText("您有 $dueCount 个项目待复习")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(REVIEW_NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                REVIEW_CHANNEL_ID,
                "复习提醒",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "定时提醒待复习项目"
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val REVIEW_CHANNEL_ID = "review_channel"
        const val REVIEW_NOTIFICATION_ID = 2

        fun calculateDelayToTarget(targetHour: Int = 9): Long {
            val now = java.util.Calendar.getInstance()
            val target = java.util.Calendar.getInstance().apply {
                set(java.util.Calendar.HOUR_OF_DAY, targetHour)
                set(java.util.Calendar.MINUTE, 0)
                set(java.util.Calendar.SECOND, 0)
                set(java.util.Calendar.MILLISECOND, 0)
            }
            if (target.timeInMillis <= now.timeInMillis) {
                target.add(java.util.Calendar.DAY_OF_MONTH, 1)
            }
            return target.timeInMillis - now.timeInMillis
        }

        fun schedule(context: Context) {
            val workRequest = androidx.work.PeriodicWorkRequestBuilder<ReviewReminderWorker>(1, java.util.concurrent.TimeUnit.DAYS)
                .setInitialDelay(calculateDelayToTarget(), java.util.concurrent.TimeUnit.MILLISECONDS)
                .build()
            androidx.work.WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "review_reminder",
                androidx.work.ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        }
    }
}