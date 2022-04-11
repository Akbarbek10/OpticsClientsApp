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
import java.text.SimpleDateFormat
import java.util.*


class ReminderBirthdayBroadcast : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
            setNotification(context, intent)
    }

    private fun setNotification(context: Context, intent: Intent) {
        val date = intent.getStringExtra("_date")



        if(date != null && isTodayBirthday(date)){
            val id = intent.getIntExtra("_id", 0)
            val title = intent.getStringExtra("_title")
            val message = intent.getStringExtra("_message")

            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent =
                PendingIntent.getActivity(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT)

            val notification = NotificationCompat.Builder(context, App.CHANNEL_BIRTHDAYS_ID)
                .setSmallIcon(R.drawable.ic_cake)
                .setContentTitle(title)
                .setContentText(message)
                .setColor(Color.parseColor("#00786D"))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build()

            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(id, notification)
        }
    }

    private fun isTodayBirthday(date: String): Boolean {
        val dateOfBirth = SimpleDateFormat("dd.MM.yyyy").parse(date)
        val cal = Calendar.getInstance()
        val currentDay = cal.get(Calendar.DAY_OF_MONTH)
        val currentMonth = cal.get(Calendar.MONTH)

        cal.time = dateOfBirth
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val month = cal.get(Calendar.MONTH)

        return (day == currentDay && month == currentMonth)
    }


}

