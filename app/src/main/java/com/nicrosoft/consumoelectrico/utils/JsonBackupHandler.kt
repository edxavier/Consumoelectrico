@file:Suppress("BlockingMethodInNonBlockingContext")

package com.nicrosoft.consumoelectrico.utils

import android.util.Log
import com.nicrosoft.consumoelectrico.data.BackupSkeleton
import com.nicrosoft.consumoelectrico.data.DateJsonAdapter
import com.nicrosoft.consumoelectrico.data.daos.ElectricMeterDAO
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.util.*


object JsonBackupHandler {
    suspend fun createBackup(dao: ElectricMeterDAO, filePathName:String){
        val moshi = Moshi.Builder()
                .add(Date::class.java, DateJsonAdapter())
                .build()
        val jsonAdapter: JsonAdapter<BackupSkeleton> = moshi.adapter<BackupSkeleton>(BackupSkeleton::class.java)

        val backup  = BackupSkeleton()
            backup.meters = dao.getForBackupMeterList()
            backup.prices = dao.getForBackupPricesList()
            backup.periods = dao.getForBackupPeriodList()
            backup.readings = dao.getForBackupReadingList()
            val json = jsonAdapter.toJson(backup)

        //val file = File(filePathName)
        val fileWriter = FileWriter("$filePathName.json")
        val bufferedWriter = BufferedWriter(fileWriter)
        bufferedWriter.write(json)
        bufferedWriter.flush()
        bufferedWriter.close()
            Log.e("EDER", json)
    }
}