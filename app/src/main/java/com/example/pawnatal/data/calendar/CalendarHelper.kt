package com.example.pawnatal.data.calendar

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.CalendarContract
import com.example.pawnatal.data.model.ScheduledMilestone
import com.example.pawnatal.data.model.VetAppointment
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object CalendarHelper {

    fun addVetAppointmentToCalendar(context: Context, appointment: VetAppointment, damName: String) {
        val title = "PawNatal: ${appointment.title} ($damName)"
        val description = "Veterinarian: ${appointment.doctorName}\nClinic: ${appointment.clinicName}\nNotes: ${appointment.notes}"
        val location = "${appointment.clinicName}, ${appointment.address}"

        val calendar = parseDateOrFallback(appointment.isoDate)

        try {
            val intent = Intent(Intent.ACTION_INSERT).apply {
                data = CalendarContract.Events.CONTENT_URI
                putExtra(CalendarContract.Events.TITLE, title)
                putExtra(CalendarContract.Events.DESCRIPTION, description)
                putExtra(CalendarContract.Events.EVENT_LOCATION, location)
                putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, calendar.timeInMillis)
                putExtra(CalendarContract.EXTRA_EVENT_END_TIME, calendar.timeInMillis + (60 * 60 * 1000))
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback to Google Calendar Web Direct Intent
            openGoogleCalendarWeb(context, title, description, location, calendar.timeInMillis)
        }
    }

    fun addMilestoneToCalendar(context: Context, milestone: ScheduledMilestone, damName: String, estimatedDate: String) {
        val title = "PawNatal: ${milestone.title} ($damName)"
        val description = "${milestone.description}\nWindow: ${milestone.dayWindow}"
        val calendar = parseDateOrFallback(estimatedDate)

        try {
            val intent = Intent(Intent.ACTION_INSERT).apply {
                data = CalendarContract.Events.CONTENT_URI
                putExtra(CalendarContract.Events.TITLE, title)
                putExtra(CalendarContract.Events.DESCRIPTION, description)
                putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, calendar.timeInMillis)
                putExtra(CalendarContract.EXTRA_EVENT_END_TIME, calendar.timeInMillis + (60 * 60 * 1000))
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            openGoogleCalendarWeb(context, title, description, "", calendar.timeInMillis)
        }
    }

    private fun openGoogleCalendarWeb(
        context: Context,
        title: String,
        details: String,
        location: String,
        timeMillis: Long
    ) {
        try {
            val sdf = SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'", Locale.US)
            val startTime = sdf.format(timeMillis)
            val endTime = sdf.format(timeMillis + 3600000L)

            val url = "https://calendar.google.com/calendar/render?action=TEMPLATE" +
                    "&text=${URLEncoder.encode(title, "UTF-8")}" +
                    "&dates=$startTime/$endTime" +
                    "&details=${URLEncoder.encode(details, "UTF-8")}" +
                    "&location=${URLEncoder.encode(location, "UTF-8")}"

            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (ignored: Exception) {}
    }

    private fun parseDateOrFallback(isoDate: String): Calendar {
        val cal = Calendar.getInstance()
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val date = sdf.parse(isoDate)
            if (date != null) {
                cal.time = date
                cal.set(Calendar.HOUR_OF_DAY, 10)
                cal.set(Calendar.MINUTE, 0)
                return cal
            }
        } catch (e: Exception) {}
        return cal
    }
}
