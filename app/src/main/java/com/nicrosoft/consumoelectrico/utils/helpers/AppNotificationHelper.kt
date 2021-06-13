package com.nicrosoft.consumoelectrico.utils.helpers

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.nicrosoft.consumoelectrico.MainKt
import com.nicrosoft.consumoelectrico.R

object AppNotificationHelper {
    const val CHANNEL_ID = "10001"
    const val CHANNEL_NAME = "CEH"

    fun sendNotification(ctx:Context, notificationId:Int, title:String, message:String){
        val resultIntent = Intent(ctx, MainKt::class.java)
        resultIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        val resultPendingIntent = PendingIntent.getActivity(ctx, 0 , resultIntent, PendingIntent.FLAG_UPDATE_CURRENT)


        val bigTextStyle = NotificationCompat.BigTextStyle()
                .bigText(message)
                .setBigContentTitle(title)


        val notificationManager = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val builder = NotificationCompat.Builder(ctx, CHANNEL_ID).also {
            it.setSmallIcon(R.drawable.ic_colored_bulb) // Icon shown at the status bar
            it.setContentTitle(title)
            it.setAutoCancel(true)
            it.setContentText(message)
            it.setContentIntent(resultPendingIntent)
            it.setStyle(bigTextStyle)
            it.priority = NotificationCompat.PRIORITY_DEFAULT
            it.setLights(Color.BLUE, 500, 500)
            it.setDefaults(Notification.DEFAULT_ALL)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
            channel.enableLights(true)
            channel.lightColor = Color.YELLOW
            channel.setShowBadge(true)
            notificationManager.createNotificationChannel(channel)
            builder.setChannelId(CHANNEL_ID)
        }
        notificationManager.notify(notificationId, builder.build())
        //val notificationId =  System.currentTimeMillis().toInt()


    }
}