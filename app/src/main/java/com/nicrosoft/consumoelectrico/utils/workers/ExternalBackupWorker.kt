package com.nicrosoft.consumoelectrico.utils.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.nicrosoft.consumoelectrico.R
import com.nicrosoft.consumoelectrico.utils.helpers.AppNotificationHelper
import com.pixplicity.easyprefs.library.Prefs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.joda.time.Days
import org.joda.time.LocalDate
import org.joda.time.LocalDateTime
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import java.text.SimpleDateFormat
import java.util.*

class ExternalBackupWorker (private val ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params), KodeinAware {
    override val kodein by kodein { ctx }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val lastExternal =  Prefs.getString("last_external_backup", "")
        val reminderEnabled =  Prefs.getBoolean("backup_reminder_enabled", true)
        if (lastExternal.isNotEmpty() and reminderEnabled){
            try {
                val sdf = SimpleDateFormat(ctx.getString(R.string.backup_date_format), Locale.getDefault())
                val lastBackup = sdf.parse(lastExternal)
                val days = Days.daysBetween(LocalDate(lastBackup), LocalDate(Date()))
                if (days.days>=30){
                    val hour = LocalDateTime.now().hourOfDay
                    //Si estamos entre las 7 am y las 9 pm
                    if (hour in 7..21) {
                        AppNotificationHelper
                                .sendNotification(ctx, 100, ctx.getString(R.string.reminder),
                                        ctx.getString(R.string.backup_notification_suggestion1))
                    }
                }
            }catch (e:Exception){}
        }else{
            AppNotificationHelper
                    .sendNotification(ctx,100, ctx.getString(R.string.reminder),
                            ctx.getString(R.string.backup_notification_suggestion2))
        }

        return@withContext Result.success()
    }

}