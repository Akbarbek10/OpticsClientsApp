package com.example.opticsclientsapp.notifications

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.opticsclientsapp.R
import com.example.opticsclientsapp.activities.MainActivity


class ReminderCallsBroadcast : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        setNotification(context, intent)
    }

    private fun setNotification(context: Context, intent: Intent) {
        val id = intent.getIntExtra("id", 0)
        val title = intent.getStringExtra("title")
        val message = intent.getStringExtra("message")

        val intent = Intent(context, MainActivity::class.java)
        intent.putExtra("key_notification_call", "from_notification")
        val pendingIntent =
            PendingIntent.getActivity(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val notification = NotificationCompat.Builder(context, App.CHANNEL_CALLS_ID)
            .setSmallIcon(R.drawable.ic_glasses)
            .setContentTitle(title)
            .setContentText(message)
            .setDefaults(NotificationCompat.DEFAULT_SOUND)
            .setColor(Color.parseColor("#00786D"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(id, notification)

    }

}

