@file:Suppress("BlockingMethodInNonBlockingContext")

package com.nicrosoft.consumoelectrico.utils.handlers

import android.content.Context
import android.net.Uri
import com.nicrosoft.consumoelectrico.data.BackupSkeleton
import com.nicrosoft.consumoelectrico.data.DateJsonAdapter
import com.nicrosoft.consumoelectrico.data.entities.ElectricBillPeriod
import com.nicrosoft.consumoelectrico.data.entities.ElectricReading
import com.nicrosoft.consumoelectrico.utils.AppResult
import com.nicrosoft.consumoelectrico.utils.helpers.BackupDatabaseHelper
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.util.Date


object JsonBackupHandler {
    suspend fun restoreBackup(backup: BackupDatabaseHelper, fileUri: Uri, context: Context): AppResult {
        try{
            val inputStream = context.contentResolver.openInputStream(fileUri)
            val reader = BufferedReader(
                InputStreamReader(
                    inputStream
                )
            )
            val stringBuilder = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                stringBuilder.append(line)
            }
            inputStream?.close()

            val moshi = Moshi.Builder()
                .add(Date::class.java, DateJsonAdapter())
                .addLast(KotlinJsonAdapterFactory())
                .build()
            val jsonAdapter: JsonAdapter<BackupSkeleton> = moshi.adapter(BackupSkeleton::class.java)
            val data = jsonAdapter.fromJson(stringBuilder.toString())
            //Log.e("EDER", outputStringBuffer.toString())
            data?.let {
                backup.saveMeters(it.meters)
                backup.savePrices(it.prices)
                backup.savePeriods(it.periods)
                backup.saveReadings(it.readings)
            }

            return AppResult.OK
        }catch (e:Exception){
            return AppResult.AppException(e)
        }
    }

    suspend fun createBackup(backupHelper:BackupDatabaseHelper, fileUri: Uri, context: Context): AppResult {
        try{
            val dao = backupHelper.getDao()
            val moshi = Moshi.Builder()
                .add(Date::class.java, DateJsonAdapter())
                .addLast(KotlinJsonAdapterFactory())
                .build()
            val jsonAdapter: JsonAdapter<BackupSkeleton> = moshi.adapter(BackupSkeleton::class.java)

            val backup  = BackupSkeleton()
            backup.meters = dao.getMeterList().toMutableList()
            backup.prices = dao.getPricesList().toMutableList()
            backup.periods = removeInfiniteNumbersPeriod(dao.getPeriodList().toMutableList())
            backup.readings = removeInfiniteNumbersReading(dao.getReadingList().toMutableList())
            val json = jsonAdapter.toJson(backup)

            val outputStream = context.contentResolver.openOutputStream(fileUri)
            val bufferedWriter = BufferedWriter(
                OutputStreamWriter(outputStream)
            )
            bufferedWriter.write(json)
            bufferedWriter.flush()
            bufferedWriter.close()
            return AppResult.OK
        }
        catch (e:Exception){ return AppResult.AppException(e) }
    }

    private fun removeInfiniteNumbersReading(readings: MutableList<ElectricReading>):MutableList<ElectricReading>{
        val iterator = readings.iterator()
        while (iterator.hasNext()) {
            val reading = iterator.next()
            if (reading.readingValue.isInfinite() || reading.readingValue.isNaN()){
                iterator.remove()
            }
            else if (reading.kwConsumption.isInfinite() || reading.kwConsumption.isNaN()){
                iterator.remove()
            }
            else if (reading.kwAvgConsumption.isInfinite() || reading.kwAvgConsumption.isNaN()){
                iterator.remove()
            }
            else if (reading.kwAggConsumption.isInfinite() || reading.kwAggConsumption.isNaN()){
                iterator.remove()
            }
            else if (reading.consumptionHours.isInfinite() || reading.consumptionHours.isNaN()){
                iterator.remove()
            }
            else if (reading.consumptionPreviousHours.isInfinite() || reading.consumptionPreviousHours.isNaN()){
                iterator.remove()
            }
        }
        return readings
    }

    private fun removeInfiniteNumbersPeriod(periods: MutableList<ElectricBillPeriod>):MutableList<ElectricBillPeriod>{
        val iterator = periods.iterator()
        while (iterator.hasNext()) {
            val period = iterator.next()
            if (period.totalKw.isInfinite() || period.totalKw.isNaN()){
                iterator.remove()
            }
            else if (period.totalBill.isInfinite() || period.totalBill.isNaN()){
                iterator.remove()
            }
        }
        return periods
    }

}