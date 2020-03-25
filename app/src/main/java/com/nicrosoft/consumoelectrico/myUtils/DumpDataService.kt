package com.nicrosoft.consumoelectrico.myUtils

import android.util.Log
import com.firebase.jobdispatcher.JobParameters
import com.firebase.jobdispatcher.JobService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class DumpDataService : JobService(), CoroutineScope {
    private lateinit var coroutineJob: Job

    override val coroutineContext: CoroutineContext
        get() = coroutineJob + Dispatchers.Main

    override fun onStartJob(job: JobParameters): Boolean {
        // Answers the question: "Is there still work going on?"
        val finalName = "com_nicrosoft_consumoelectrico_BACKUP"
        val defPath = RestoreHelper.getInternalStoragePath(applicationContext)
        launch {
            try {
                CSVHelper.saveAllToCSV(defPath, finalName, applicationContext)
            }catch (e:Exception){}
        }
        return true
    }

    override fun onStopJob(job: JobParameters): Boolean {
        // si el trabajo falla retornar true para reintentar
        return true
    }

    companion object {
        const val JOB_TAG = "DumpDataService"
    }

    override fun onCreate() {
        coroutineJob = Job()
        super.onCreate()
    }

    override fun onDestroy() {
        coroutineJob.cancel()
        super.onDestroy()
    }
}