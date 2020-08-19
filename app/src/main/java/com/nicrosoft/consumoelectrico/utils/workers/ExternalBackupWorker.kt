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
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance
import java.util.*
import kotlin.coroutines.CoroutineContext

class ExternalBackupWorker (private val ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params), KodeinAware {
    override val kodein by kodein { ctx }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        Log.e("EDER", "ExternalBackupWorker")
        val lastExternal =  Prefs.getString("last_external_backup", "")
        if (lastExternal.isNotEmpty()){
            AppNotificationHelper.sendNotification(ctx, ctx.getString(R.string.reminder), "Hace varios dias que no realiza un respaldo, es recomendable realizar una copia de seguridad de los datos para evitar perdida de informacion")
        }else{
            AppNotificationHelper.sendNotification(ctx, ctx.getString(R.string.reminder), "Parece que nunca ha realizado un respaldo manual, es recomendable realizar copias de seguridad de los datos de manera frecuente para evitar perdida de informacion")
        }

        return@withContext Result.success()
    }

}