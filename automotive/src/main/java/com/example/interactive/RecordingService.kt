package com.example.interactive

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.IBinder

/**
 * 화면이 가려져도 앱 프로세스가 꺼지지 않게 붙들어 두는 포그라운드 서비스.
 * 기록 자체는 RecordingEngine이 하고, 이 서비스는 시작할 때 엔진을 깨우기만 한다.
 */
class RecordingService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, getString(R.string.recording_channel), NotificationManager.IMPORTANCE_LOW)
        )
        val notification = Notification.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(getString(R.string.recording_notice))
            .setOngoing(true)
            .build()
        startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        (application as InteractiveApp).engine // 엔진을 만들어 기록을 시작한다
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private companion object {
        const val CHANNEL_ID = "recording"
        const val NOTIFICATION_ID = 1
    }
}
