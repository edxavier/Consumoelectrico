package com.nicrosoft.consumoelectrico.utils.helpers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationCompat
import com.nicrosoft.consumoelectrico.MainKt
import com.nicrosoft.consumoelectrico.R

class NotificationHelper(private val mContext: Context) {
    private var mNotificationManager: NotificationManager? = null
    private var mBuilder: NotificationCompat.Builder? = null

    /**
     * Create and push the notification
     */
    fun createNotification(title: String?, message: String?) {
        /**Creates an explicit intent for an Activity in your app */
        val resultIntent = Intent(mContext, MainKt::class.java)
        //resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val resultPendingIntent = PendingIntent.getActivity(mContext,
                10 /* Request code */, resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT)
        mBuilder = NotificationCompat.Builder(mContext)
        mBuilder!!.setSmallIcon(R.drawable.ic_bulb)
        mBuilder!!.setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(false)
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                .setContentIntent(resultPendingIntent)
        mNotificationManager = mContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val notificationChannel = NotificationChannel(NOTIFICATION_CHANNEL_ID, "NOTIFICATION_CHANNEL_NAME", importance)
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.enableVibration(true)
            notificationChannel.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
            mBuilder!!.setChannelId(NOTIFICATION_CHANNEL_ID)
            mNotificationManager!!.createNotificationChannel(notificationChannel)
        }
        mNotificationManager!!.notify(0 /* Request Code */, mBuilder!!.build())
    }

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "10001"
    }

}