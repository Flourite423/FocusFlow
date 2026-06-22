package com.focusflow.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.focusflow.R
import com.focusflow.data.db.FocusFlowDatabase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.LocalDate
import java.time.ZoneId
import java.util.concurrent.TimeUnit

@HiltWorker
class ReviewReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val db: FocusFlowDatabase
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val today = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val dueCount = db.reviewScheduleDao().getDueReviewCountSync(today)

        if (dueCount > 0) {
            sendNotification("复习提醒", "你有 $dueCount 项待复习内容")
        }
        return Result.success()
    }

    private fun sendNotification(title: String, message: String) {
        val nm = applicationContext.getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(CHANNEL_ID, "复习提醒", NotificationManager.IMPORTANCE_DEFAULT)
        nm.createNotificationChannel(channel)
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_review)
            .setAutoCancel(true)
            .build()
        nm.notify(NOTIFICATION_ID, notification)
    }

    companion object {
        private const val CHANNEL_ID = "review_channel"
        private const val NOTIFICATION_ID = 2
        private const val WORK_NAME = "review_reminder"

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<ReviewReminderWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(calculateDelayToTarget(), TimeUnit.MILLISECONDS)
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }

        private fun calculateDelayToTarget(targetHour: Int = 9): Long {
            val now = java.time.LocalDateTime.now()
            var target = now.toLocalDate().atTime(targetHour, 0)
            if (now.isAfter(target)) target = target.plusDays(1)
            return java.time.Duration.between(now, target).toMillis()
        }
    }
}
