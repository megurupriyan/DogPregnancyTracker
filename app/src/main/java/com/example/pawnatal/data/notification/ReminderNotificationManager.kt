package com.example.pawnatal.data.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.pawnatal.MainActivity
import java.util.Locale

class ReminderNotificationManager(private val context: Context) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannels()
    }

    fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val routineChannel = NotificationChannel(
                CHANNEL_ROUTINE_ID,
                "PawNatal Care & Daily Routines",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Daily morning reminders for prenatal nutrition, supplements, and milestone progression."
            }

            val tempChannel = NotificationChannel(
                CHANNEL_TEMP_ID,
                "PawNatal Basal Temperature Tracking",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Daily evening reminders for rectal temperature logs to catch pre-labor hypothermia."
            }

            val urgentChannel = NotificationChannel(
                CHANNEL_URGENT_ID,
                "PawNatal Pre-Labor & Critical Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Critical alerts for pre-labor hypothermia drops (< 37.2°C / 99.0°F) and urgent veterinary milestones."
                enableVibration(true)
            }

            notificationManager.createNotificationChannel(routineChannel)
            notificationManager.createNotificationChannel(tempChannel)
            notificationManager.createNotificationChannel(urgentChannel)
        }
    }

    fun sendHypothermiaAlert(damName: String, tempF: Double) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            1001,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val tempC = (tempF - 32.0) * 5.0 / 9.0
        val cFormatted = String.format(Locale.ENGLISH, "%.1f°C", tempC)
        val fFormatted = String.format(Locale.ENGLISH, "%.1f°F", tempF)

        val notification = NotificationCompat.Builder(context, CHANNEL_URGENT_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("🚨 Pre-Labor Drop Detected: $cFormatted ($fFormatted)")
            .setContentText("${damName}'s temperature dropped below 37.2°C (99.0°F)! Progesterone plummet indicates whelping begins within 12–24h.")
            .setStyle(NotificationCompat.BigTextStyle().bigText(
                "${damName}'s rectal temperature dropped to $cFormatted ($fFormatted). This signals progesterone collapse and impending active labor within 12–24 hours.\n\n" +
                        "Urgent action items:\n" +
                        "• Move ${damName} into the sanitized whelping box.\n" +
                        "• Verify whelping kit & pre-warm newborn den heating pad.\n" +
                        "• Alert your attending veterinarian or emergency clinic."
            ))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(NOTIFICATION_ID_URGENT, notification)
    }

    fun sendMorningRoutineReminder(damName: String, dayOfGestation: Int, milestoneTitle: String, pendingMedsCount: Int) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            1002,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val medsText = if (pendingMedsCount > 0) " • $pendingMedsCount supplement(s) due" else ""
        val contentText = "Gestation Day $dayOfGestation of 63. Today's focus: $milestoneTitle$medsText."

        val notification = NotificationCompat.Builder(context, CHANNEL_ROUTINE_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("🐾 PawNatal: Day $dayOfGestation • $damName")
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(
                "Good morning! $damName is on Gestation Day $dayOfGestation of 63.\n\n" +
                        "• Stage: $milestoneTitle\n" +
                        "• Morning routine: Check nutrition, provide fresh water, and administer scheduled prenatal vitamins.\n" +
                        "• Open PawNatal to mark today's routine tasks."
            ))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(NOTIFICATION_ID_MORNING, notification)
    }

    fun sendEveningTempReminder(damName: String, dayOfGestation: Int, isLateGestation: Boolean) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            1003,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val urgencyText = if (isLateGestation) {
            "Late gestation alert: Pre-labor drop below 37.2°C (99.0°F) signals whelping within 12–24h."
        } else {
            "Maintain baseline temperature records to accurately establish $damName's normal curve."
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_TEMP_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("🌡️ Evening Basal Temp Check • $damName")
            .setContentText("Record $damName's evening rectal temperature (Day $dayOfGestation). $urgencyText")
            .setStyle(NotificationCompat.BigTextStyle().bigText(
                "Evening rectal temperature check for $damName (Day $dayOfGestation of 63).\n\n" +
                        "$urgencyText\n\n" +
                        "Normal safe baseline: 37.7°C–38.4°C (99.8°F–101.2°F). Tap to record reading now."
            ))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(NOTIFICATION_ID_EVENING, notification)
    }

    fun sendTestReminder() {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            1004,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ROUTINE_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("✅ PawNatal Background Reminders Active")
            .setContentText("Alarms and background workers are operating normally even when PawNatal is closed or killed.")
            .setStyle(NotificationCompat.BigTextStyle().bigText(
                "PawNatal Background Notifications Verification:\n\n" +
                        "• Morning Care & Routine Alarm: 08:30 AM (Daily)\n" +
                        "• Evening Basal Temp Alarm: 20:00 (8:00 PM Daily)\n" +
                        "• Pre-Labor Critical Alert: Immediate trigger on temp < 37.2°C\n\n" +
                        "Notifications persist across app termination and system reboots via AlarmManager & BootReceiver."
            ))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(NOTIFICATION_ID_TEST, notification)
    }

    companion object {
        const val CHANNEL_ROUTINE_ID = "pawnatal_routine_channel"
        const val CHANNEL_TEMP_ID = "pawnatal_temp_channel"
        const val CHANNEL_URGENT_ID = "pawnatal_urgent_channel"

        const val NOTIFICATION_ID_URGENT = 9901
        const val NOTIFICATION_ID_MORNING = 9902
        const val NOTIFICATION_ID_EVENING = 9903
        const val NOTIFICATION_ID_TEST = 9904
    }
}
