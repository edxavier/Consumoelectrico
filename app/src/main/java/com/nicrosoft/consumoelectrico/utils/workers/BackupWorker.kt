package com.nicrosoft.consumoelectrico.utils.workers

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.nicrosoft.consumoelectrico.utils.backupFormat
import com.nicrosoft.consumoelectrico.utils.handlers.JsonBackupHandler
import com.nicrosoft.consumoelectrico.utils.helpers.BackupDatabaseHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.kodein.di.DIAware
import org.kodein.di.android.closestDI
import org.kodein.di.instance
import java.util.*

class BackupWorker (private val ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params), DIAware {
    override val di by closestDI { ctx }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            val backupHelper: BackupDatabaseHelper by instance()
            val dir = ctx.getExternalFilesDir("AutoBackups")
            val name = "AUTO_BACKUP " + Date().backupFormat(ctx)
            val filePath = "$dir/$name"
            //No incluir extension en la ruta
            JsonBackupHandler.createBackup(backupHelper, filePath)
            //Log.e("EDER", "doWork BACKUP")
        }
        return@withContext Result.success()
    }

}