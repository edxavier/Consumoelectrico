@file:Suppress("BlockingMethodInNonBlockingContext")

package com.nicrosoft.consumoelectrico.utils.handlers

import android.util.Log
import com.nicrosoft.consumoelectrico.data.BackupSkeleton
import com.nicrosoft.consumoelectrico.data.DateJsonAdapter
import com.nicrosoft.consumoelectrico.data.daos.ElectricMeterDAO
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import java.io.*
import java.util.*


object JsonBackupHandler {
    suspend fun createBackup(dao: ElectricMeterDAO, filePathName:String): Boolean {
        try{
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
            return true
        }
        catch (e:Exception){ return false }
    }

    suspend fun restoreBackup(dao: ElectricMeterDAO, filePathName:String): Boolean {
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
        Log.e("EDER", data?.meters.toString())
        return true
    }
}