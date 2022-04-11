package com.example.opticsclientsapp.notifications

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build


class App : Application() {

    override fun onCreate() {
        super.onCreate()

        createNotificationChannels()
    }


    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel_call = NotificationChannel(CHANNEL_CALLS_ID, "Users notification channel for Calls", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "This is Channel for Calls"
            }
            val channel_birthday = NotificationChannel(CHANNEL_BIRTHDAYS_ID, "Users notification channel for Birthdays", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "This is Channel for Birthdays"
            }

            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel_call)
            manager.createNotificationChannel(channel_birthday)
        }

    }


    companion object {
        const val CHANNEL_CALLS_ID = "channel for calls"
        const val CHANNEL_BIRTHDAYS_ID = "channel for birthdays"
    }
}