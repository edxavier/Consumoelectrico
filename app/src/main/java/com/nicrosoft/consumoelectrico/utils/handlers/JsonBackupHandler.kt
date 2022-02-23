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
            backup.meters = dao.getMeterList().toMutableList()
            backup.prices = dao.getPricesList().toMutableList()
            backup.periods = dao.getPeriodList().toMutableList()
            backup.readings = dao.getReadingList().toMutableList()
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
        readings.forEach { reading ->
            if (reading.readingValue.isInfinite() || reading.readingValue.isNaN()){
                readings.remove(reading)
            }
            else if (reading.kwConsumption.isInfinite() || reading.kwConsumption.isNaN()){
                readings.remove(reading)
            }
            else if (reading.kwAvgConsumption.isInfinite() || reading.kwAvgConsumption.isNaN()){
                readings.remove(reading)
            }
            else if (reading.kwAggConsumption.isInfinite() || reading.kwAggConsumption.isNaN()){
                readings.remove(reading)
            }
            else if (reading.consumptionHours.isInfinite() || reading.consumptionHours.isNaN()){
                readings.remove(reading)
            }
            else if (reading.consumptionPreviousHours.isInfinite() || reading.consumptionPreviousHours.isNaN()){
                readings.remove(reading)
            }
        }
        return readings
    }

    private fun removeInfiniteNumbersPeriod(periods: MutableList<ElectricBillPeriod>):MutableList<ElectricBillPeriod>{
        periods.forEach { period ->
            if (period.totalKw.isInfinite() || period.totalKw.isNaN()){
                periods.remove(period)
            }
            else if (period.totalBill.isInfinite() || period.totalBill.isNaN()){
                periods.remove(period)
            }

        }
        return periods
    }

}