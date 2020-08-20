package com.nicrosoft.consumoelectrico.utils.workers

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.nicrosoft.consumoelectrico.R
import com.nicrosoft.consumoelectrico.data.daos.ElectricMeterDAO
import com.nicrosoft.consumoelectrico.utils.backupFormat
import com.nicrosoft.consumoelectrico.utils.handlers.JsonBackupHandler
import com.nicrosoft.consumoelectrico.utils.helpers.AppNotificationHelper
import com.nicrosoft.consumoelectrico.utils.helpers.BackupDatabaseHelper
import com.nicrosoft.consumoelectrico.utils.helpers.NotificationHelper
import com.pixplicity.easyprefs.library.Prefs
import kotlinx.coroutines.*
import org.joda.time.Days
import org.joda.time.LocalDate
import org.joda.time.LocalDateTime
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance
import java.util.*
import kotlin.coroutines.CoroutineContext

class ReadReminderWorker (private val ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params), KodeinAware {
    override val kodein by kodein { ctx }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        //Log.e("EDER", "ReadReminderWorker")
        launch {
            val backupHelper: BackupDatabaseHelper by instance()
            val dao = backupHelper.getDao()
            val meters = dao.getMeterList()
            if (meters.isNotEmpty()){
                meters.forEach {m->
                    val latestReading = dao.getLatestReading(m.code)
                    latestReading?.let{ r->
                        val timePassed = Days.daysBetween(LocalDate(r.readingDate), LocalDate(Date()))
                        if (timePassed.days > m.readReminder){
                            val hour = LocalDateTime.now().hourOfDay
                            //Si estamos entre las 7 am y las 9 pm
                            if (hour in 7..21) {
                                val title = ctx.getString(R.string.reminder) + " " + m.name
                                AppNotificationHelper
                                        .sendNotification(ctx, m.id!!,  title,
                                                ctx.getString(R.string.reminder_msg, timePassed.days.toString()))
                            }
                        }
                    }
                }
            }
        }
        return@withContext Result.success()
    }

}