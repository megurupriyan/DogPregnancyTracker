package com.example.pawnatal

import android.app.Application
import android.content.Intent
import android.content.IntentFilter
import com.example.pawnatal.data.notification.AlarmScheduler
import com.example.pawnatal.data.notification.DateChangeReceiver
import com.example.pawnatal.data.notification.ReminderNotificationManager

class PawNatalApp : Application() {

    private val dateChangeReceiver = DateChangeReceiver()

    override fun onCreate() {
        super.onCreate()

        // 1. Initialize notification channels early
        ReminderNotificationManager(this).createNotificationChannels()

        // 2. Schedule daily background alarms
        AlarmScheduler.scheduleDailyReminders(this)

        // 3. Register dynamic system date change listener
        val dateFilter = IntentFilter().apply {
            addAction(Intent.ACTION_DATE_CHANGED)
            addAction(Intent.ACTION_TIME_CHANGED)
            addAction(Intent.ACTION_TIMEZONE_CHANGED)
        }
        registerReceiver(dateChangeReceiver, dateFilter)
    }
}
