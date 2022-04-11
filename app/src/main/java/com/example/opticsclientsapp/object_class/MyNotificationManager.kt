package com.example.opticsclientsapp.object_class

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.example.opticsclientsapp.R
import com.example.opticsclientsapp.activities.MainActivity
import com.example.opticsclientsapp.models.Client
import com.example.opticsclientsapp.notifications.ReminderBirthdayBroadcast
import com.example.opticsclientsapp.notifications.ReminderCallsBroadcast
import java.text.SimpleDateFormat
import java.util.concurrent.TimeUnit

object MyNotificationManager {

    fun createCallsNotification(context: Context, client: Client) {
        val id = client.id
        val title = context.getString(R.string.notification_txt)
        val message = client.fullname + context.getString(R.string.notification_message)

        val notificationDateInMillis =
            getNotificationDateInMillis(client.dateOfPurchase, client.wearingTime)

        val intent = Intent(context, ReminderCallsBroadcast::class.java)
        intent.putExtra("id", id)
        intent.putExtra("title", title)
        intent.putExtra("message", message)
        val pendingIntent =
            PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val alarmManager =
            context.getSystemService(AppCompatActivity.ALARM_SERVICE) as AlarmManager


        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                notificationDateInMillis,
                pendingIntent
            )
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, notificationDateInMillis, pendingIntent)
        }
    }

    fun createBirthdaysNotification(context: Context, client: Client) {
        val id = client.id
        val title = context.getString(R.string.notification_txt)
        val message = context.getString(R.string.notification_birthday_message, client.fullname)
        val dateOfBirth = client.dateOfBirth

        val notifyAt = SimpleDateFormat(MainActivity.MY_DATE_FORMAT).parse(dateOfBirth).time

        val intent = Intent(context, ReminderBirthdayBroadcast::class.java)
        intent.putExtra("_id", id)
        intent.putExtra("_title", title)
        intent.putExtra("_message", message)
        intent.putExtra("_date", dateOfBirth)

        val pendingIntent =
            PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val alarmManager =
            context.getSystemService(AppCompatActivity.ALARM_SERVICE) as AlarmManager
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            notifyAt,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    fun deleteCallNotification(context: Context, id: Int) {
        val alarmManager = context.getSystemService(AppCompatActivity.ALARM_SERVICE) as AlarmManager
        val intentCall = Intent(context, ReminderCallsBroadcast::class.java)

        val pendingIntentCall =
            PendingIntent.getBroadcast(context, id, intentCall, 0)

        alarmManager.cancel(pendingIntentCall)
    }

    fun deleteBirthdayNotification(context: Context, id: Int) {
        val alarmManager = context.getSystemService(AppCompatActivity.ALARM_SERVICE) as AlarmManager
        val intentBirthday = Intent(context, ReminderBirthdayBroadcast::class.java)

        val pendingIntentBirthday =
            PendingIntent.getBroadcast(context, id, intentBirthday, 0)

        alarmManager.cancel(pendingIntentBirthday)
    }


    fun getNotificationDateInMillis(dateOfPurchase: String, wearingTimeInDays: Int): Long {
        val dateOfPurchase =
            SimpleDateFormat(MainActivity.MY_DATE_FORMAT).parse(dateOfPurchase)
        val notificationDateInMillis =
            dateOfPurchase.time + TimeUnit.DAYS.toMillis(wearingTimeInDays.toLong()) + TimeUnit.HOURS.toMillis(
                12
            )

        return notificationDateInMillis
    }
}