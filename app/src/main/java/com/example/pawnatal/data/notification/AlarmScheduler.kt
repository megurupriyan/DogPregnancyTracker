package com.example.pawnatal.data.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.Calendar

object AlarmScheduler {

    const val ACTION_MORNING_REMINDER = "com.example.pawnatal.ACTION_MORNING_REMINDER"
    const val ACTION_EVENING_REMINDER = "com.example.pawnatal.ACTION_EVENING_REMINDER"
    const val ACTION_TEST_REMINDER = "com.example.pawnatal.ACTION_TEST_REMINDER"

    const val REQUEST_CODE_MORNING = 2001
    const val REQUEST_CODE_EVENING = 2002
    const val REQUEST_CODE_TEST = 2003

    fun scheduleDailyReminders(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        // 1. Morning Routine Alarm (08:30 AM)
        val morningTime = getNextTriggerMillis(hourOfDay = 8, minute = 30)
        val morningIntent = Intent(context, PregnancyAlarmReceiver::class.java).apply {
            action = ACTION_MORNING_REMINDER
            addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
        }
        val morningPendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_MORNING,
            morningIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        setAlarm(alarmManager, morningTime, morningPendingIntent)

        // 2. Evening Basal Temp Alarm (20:00 / 8:00 PM)
        val eveningTime = getNextTriggerMillis(hourOfDay = 20, minute = 0)
        val eveningIntent = Intent(context, PregnancyAlarmReceiver::class.java).apply {
            action = ACTION_EVENING_REMINDER
            addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
        }
        val eveningPendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_EVENING,
            eveningIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        setAlarm(alarmManager, eveningTime, eveningPendingIntent)
    }

    fun triggerTestNotificationNow(context: Context) {
        val notificationManager = ReminderNotificationManager(context)
        notificationManager.sendTestReminder()
    }

    fun scheduleTestAlarm(context: Context, delaySeconds: Long = 3) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val testIntent = Intent(context, PregnancyAlarmReceiver::class.java).apply {
            action = ACTION_TEST_REMINDER
            addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_TEST,
            testIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val triggerAt = System.currentTimeMillis() + (delaySeconds * 1000)
        setAlarm(alarmManager, triggerAt, pendingIntent)
    }

    fun cancelAllReminders(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val morningIntent = Intent(context, PregnancyAlarmReceiver::class.java).apply {
            action = ACTION_MORNING_REMINDER
        }
        val morningPending = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_MORNING,
            morningIntent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        morningPending?.let { alarmManager.cancel(it) }

        val eveningIntent = Intent(context, PregnancyAlarmReceiver::class.java).apply {
            action = ACTION_EVENING_REMINDER
        }
        val eveningPending = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_EVENING,
            eveningIntent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        eveningPending?.let { alarmManager.cancel(it) }
    }

    private fun setAlarm(alarmManager: AlarmManager, triggerAtMillis: Long, pendingIntent: PendingIntent) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
                } else {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
                }
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            }
        } catch (e: SecurityException) {
            // In case exact alarm permission is restricted, fallback to inexact while idle
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            }
        }
    }

    private fun getNextTriggerMillis(hourOfDay: Int, minute: Int): Long {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hourOfDay)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (target.before(now)) {
            target.add(Calendar.DAY_OF_YEAR, 1)
        }
        return target.timeInMillis
    }
}
