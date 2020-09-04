@file:Suppress("BlockingMethodInNonBlockingContext")

package com.nicrosoft.consumoelectrico.utils.handlers

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.nicrosoft.consumoelectrico.data.BackupSkeleton
import com.nicrosoft.consumoelectrico.data.DateJsonAdapter
import com.nicrosoft.consumoelectrico.utils.AppResult
import com.nicrosoft.consumoelectrico.utils.helpers.BackupDatabaseHelper
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import java.io.*
import java.util.*


object JsonBackupHandler {
    suspend fun createBackup(backupHelper:BackupDatabaseHelper, filePathName:String): AppResult {
        try{
            val dao = backupHelper.getDao()
            val moshi = Moshi.Builder()
                    .add(Date::class.java, DateJsonAdapter())
                    .build()
            val jsonAdapter: JsonAdapter<BackupSkeleton> = moshi.adapter(BackupSkeleton::class.java)

            val backup  = BackupSkeleton()
            backup.meters = dao.getMeterList()
            backup.prices = dao.getPricesList()
            backup.periods = dao.getPeriodList()
            backup.readings = dao.getReadingList()
            val json = jsonAdapter.toJson(backup)

            //val file = File(filePathName)
            val fileWriter = FileWriter("$filePathName.json")
            val bufferedWriter = BufferedWriter(fileWriter)
            bufferedWriter.write(json)
            bufferedWriter.flush()
            bufferedWriter.close()
            return AppResult.OK
        }
        catch (e:Exception){ return AppResult.AppException(e) }
    }

    suspend fun restoreBackup(backup:BackupDatabaseHelper, filePathName:String): AppResult {
        try{
            val fileReader = FileReader(filePathName)
            val outputStringBuffer = StringBuffer()
            val bufferReader = BufferedReader(fileReader)
            var line:String? = null
            while (bufferReader.readLine().also { line = it } != null){
                outputStringBuffer.append(line)
            }
            bufferReader.close()

            val moshi = Moshi.Builder().add(Date::class.java, DateJsonAdapter()).build()
            val jsonAdapter: JsonAdapter<BackupSkeleton> = moshi.adapter<BackupSkeleton>(BackupSkeleton::class.java)
            val data = jsonAdapter.fromJson(outputStringBuffer.toString())
            //Log.e("EDER", outputStringBuffer.toString())
            data?.let {
                backup.saveMeters(it.meters)
                backup.savePrices(it.prices)
                backup.savePeriods(it.periods)
                backup.saveReadings(it.readings)
            }
            //Log.e("EDER", data?.meters.toString())
            return AppResult.OK
        }catch (e:Exception){
            //Log.e("EDER -->", e.stackTraceToString())
            //FirebaseCrashlytics.getInstance().log("FALLO DE IMPORTACION")
            //FirebaseCrashlytics.getInstance().recordException(e)
            return AppResult.AppException(e)
        }
    }
}