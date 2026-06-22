package com.focusflow.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.lifecycle.MutableStateFlow
import com.focusflow.R

class TimerService : Service() {

    private val binder = TimerBinder()
    private var startTime: Long = 0
    private var pausedAt: Long = 0
    private var isPaused = false
    private val handler = Handler(Looper.getMainLooper())
    private val tickRunnable = object : Runnable {
        override fun run() {
            if (!isPaused) {
                val elapsed = (System.currentTimeMillis() - startTime) / 1000
                _elapsedSeconds.value = elapsed.toInt()
                updateNotification(elapsed.toInt())
                handler.postDelayed(this, 1000)
            }
        }
    }

    val _elapsedSeconds = MutableStateFlow(0)
    val elapsedSeconds = _elapsedSeconds

    inner class TimerBinder : android.os.Binder() {
        fun getService(): TimerService = this@TimerService
    }

    companion object {
        const val ACTION_START = "com.focusflow.service.TimerService.START"
        const val ACTION_PAUSE = "com.focusflow.service.TimerService.PAUSE"
        const val ACTION_RESUME = "com.focusflow.service.TimerService.RESUME"
        const val ACTION_STOP = "com.focusflow.service.TimerService.STOP"
        const val CHANNEL_ID = "study_timer"
        const val NOTIFICATION_ID = 1
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        when (action) {
            ACTION_START -> startTimer()
            ACTION_PAUSE -> pauseTimer()
            ACTION_RESUME -> resumeTimer()
            ACTION_STOP -> stopTimer()
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    private fun startTimer() {
        startTime = System.currentTimeMillis()
        isPaused = false
        _elapsedSeconds.value = 0
        startForeground(NOTIFICATION_ID, buildNotification(0))
        handler.post(tickRunnable)
    }

    private fun pauseTimer() {
        if (!isPaused) {
            pausedAt = System.currentTimeMillis()
            isPaused = true
            handler.removeCallbacks(tickRunnable)
        }
    }

    private fun resumeTimer() {
        if (isPaused) {
            val pauseDuration = System.currentTimeMillis() - pausedAt
            startTime += pauseDuration
            isPaused = false
            handler.post(tickRunnable)
        }
    }

    private fun stopTimer() {
        handler.removeCallbacks(tickRunnable)
        stopForeground(true)
        stopSelf()
    }

    private fun updateNotification(elapsedSeconds: Int) {
        val notification = buildNotification(elapsedSeconds)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun buildNotification(elapsedSeconds: Int): Notification {
        val hours = elapsedSeconds / 3600
        val minutes = (elapsedSeconds % 3600) / 60
        val seconds = elapsedSeconds % 60
        val timeString = if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }

        val intent = Intent(this, TimerService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_timer)
            .setContentTitle("专注计时")
            .setContentText(timeString)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .addAction(R.drawable.ic_timer, "停止", stopPendingIntent)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "学习计时器",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "显示专注计时进度"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        handler.removeCallbacks(tickRunnable)
        super.onDestroy()
    }
}