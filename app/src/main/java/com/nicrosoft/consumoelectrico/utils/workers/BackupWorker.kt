package com.nicrosoft.consumoelectrico.utils.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.nicrosoft.consumoelectrico.data.daos.ElectricMeterDAO
import kotlinx.coroutines.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance
import kotlin.coroutines.CoroutineContext

class BackupWorker (ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params), KodeinAware {
    override val kodein by kodein { ctx }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {

        val dao: ElectricMeterDAO by instance()
        Log.e("EDER", "doWork BACKUP")
        return@withContext Result.success()
    }

}