package com.focusflow.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.focusflow.MainActivity
import com.focusflow.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class TimerService : Service() {

    private val binder = TimerBinder()
    private var startTime: Long = 0
    private var pausedAt: Long = 0
    private var isPaused = false
    private val handler = Handler(Looper.getMainLooper())

    private val _elapsedSeconds = MutableStateFlow(0)
    val elapsedSeconds: StateFlow<Int> = _elapsedSeconds

    private val tickRunnable = object : Runnable {
        override fun run() {
            if (!isPaused) {
                _elapsedSeconds.value = ((System.currentTimeMillis() - startTime) / 1000).toInt()
            }
            handler.postDelayed(this, 1000)
        }
    }

    inner class TimerBinder : android.os.Binder() {
        fun getService(): TimerService = this@TimerService
    }

    companion object {
        const val ACTION_START = "com.focusflow.action.START"
        const val ACTION_PAUSE = "com.focusflow.action.PAUSE"
        const val ACTION_RESUME = "com.focusflow.action.RESUME"
        const val ACTION_STOP = "com.focusflow.action.STOP"
        private const val CHANNEL_ID = "study_timer"
        private const val NOTIFICATION_ID = 1
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startTimer()
            ACTION_PAUSE -> pauseTimer()
            ACTION_RESUME -> resumeTimer()
            ACTION_STOP -> stopTimer()
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder = binder

    private fun startTimer() {
        startTime = System.currentTimeMillis()
        isPaused = false
        startForeground(NOTIFICATION_ID, buildNotification(0))
        handler.post(tickRunnable)
    }

    private fun pauseTimer() {
        pausedAt = System.currentTimeMillis()
        isPaused = true
    }

    private fun resumeTimer() {
        startTime += System.currentTimeMillis() - pausedAt
        isPaused = false
    }

    private fun stopTimer() {
        handler.removeCallbacks(tickRunnable)
        _elapsedSeconds.value = 0
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun buildNotification(elapsedSeconds: Int): Notification {
        val timeStr = String.format("%02d:%02d", elapsedSeconds / 60, elapsedSeconds % 60)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("FocusFlow")
            .setContentText("学习中... $timeStr")
            .setSmallIcon(R.drawable.ic_timer)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(CHANNEL_ID, "学习计时", NotificationManager.IMPORTANCE_LOW)
            .apply { description = "显示学习计时状态" }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(tickRunnable)
    }
}
