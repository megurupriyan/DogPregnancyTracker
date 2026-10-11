package com.example.pawnatal.data.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.pawnatal.data.repository.PregnancyRepository

class DateChangeReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action
        if (action == Intent.ACTION_DATE_CHANGED ||
            action == Intent.ACTION_TIME_CHANGED ||
            action == Intent.ACTION_TIMEZONE_CHANGED
        ) {
            // Reconcile gestational timeline metrics for the new day
            val repository = PregnancyRepository(context.applicationContext)
            repository.refreshForToday()

            // Also invoke any registered live UI listener
            onDateChangedListener?.invoke()
        }
    }

    companion object {
        var onDateChangedListener: (() -> Unit)? = null
    }
}
