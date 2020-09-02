package com.nicrosoft.consumoelectrico.utils.helpers

import android.content.Context
import android.util.Log
import com.nicrosoft.consumoelectrico.data.daos.BackupDAO
import com.nicrosoft.consumoelectrico.data.entities.ElectricBillPeriod
import com.nicrosoft.consumoelectrico.data.entities.ElectricMeter
import com.nicrosoft.consumoelectrico.data.entities.ElectricReading
import com.nicrosoft.consumoelectrico.data.entities.PriceRange
import com.nicrosoft.consumoelectrico.realm.Lectura
import com.nicrosoft.consumoelectrico.realm.Medidor
import com.nicrosoft.consumoelectrico.realm.Periodo
import com.nicrosoft.consumoelectrico.utils.formatDate
import com.pixplicity.easyprefs.library.Prefs
import io.realm.Realm
import io.realm.Sort
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.collections.ArrayList

class BackupDatabaseHelper(val context: Context, private val dao:BackupDAO){

    fun getDao() = dao
    private suspend fun meterExist(meterCode: String):Boolean = withContext(Dispatchers.IO){
        return@withContext dao.checkMeterExist(meterCode) != null
    }
    private suspend fun priceExist(priceCode: String):Boolean = withContext(Dispatchers.IO){
        return@withContext dao.checkPriceExist(priceCode) != null
    }
    private suspend fun periodExist(periodCode: String):Boolean = withContext(Dispatchers.IO){
        return@withContext dao.checkPeriodExist(periodCode) != null
    }
    private suspend fun readingExist(readingCode: String):Boolean = withContext(Dispatchers.IO){
        return@withContext dao.checkReadingExist(readingCode) != null
    }

    suspend fun saveMeters(meters: List<ElectricMeter>)= withContext(Dispatchers.IO){
        val tempMeters:MutableList<ElectricMeter> = ArrayList()
        tempMeters.addAll(meters)
        meters.forEach {
            if(meterExist(it.code))
                tempMeters.remove(it)
        }
        dao.saveMeters(tempMeters)
    }
    suspend fun savePrices(prices: List<PriceRange>)= withContext(Dispatchers.IO){
        val tempList:MutableList<PriceRange> = ArrayList()
        tempList.addAll(prices)
        prices.forEach {
            if(priceExist(it.code))
                tempList.remove(it)
        }
        dao.savePrices(tempList)
    }
    suspend fun savePeriods(periods: List<ElectricBillPeriod>)= withContext(Dispatchers.IO){
        val tempList:MutableList<ElectricBillPeriod> = ArrayList()
        tempList.addAll(periods)
        periods.forEach {
            //Log.w("EDER", it.toString())
            if(periodExist(it.code))
                tempList.remove(it)
        }
        Log.e("EDER",  Locale.getDefault().toString())
        dao.savePeriods(tempList)
    }
    suspend fun saveReadings(readings: List<ElectricReading>)= withContext(Dispatchers.IO){
        val tempList:MutableList<ElectricReading> = ArrayList()
        tempList.addAll(readings)
        readings.forEach {
            if(readingExist(it.code))
                tempList.remove(it)
        }
        dao.saveReadings(tempList)
    }

    suspend fun tryMigration() = withContext(Dispatchers.IO){
        val realm = Realm.getDefaultInstance()

        val oldMeters = realm.where(Medidor::class.java).findAll()
        val newMeters:MutableList<ElectricMeter> = ArrayList()
        oldMeters?.forEach {
            if(!meterExist(it.id))
                newMeters.add(ElectricMeter(name = it.name, code = it.id, description = it.descripcion))
        }
        dao.saveMeters(newMeters)
        // PERIODS
        val oldPeriods = realm.where(Periodo::class.java).findAll().sort("inicio", Sort.ASCENDING)
        val newPeriods:MutableList<ElectricBillPeriod> = ArrayList()
        oldPeriods?.forEach {
            if(!periodExist(it.id)) {
                if (it.fin!=null)
                    newPeriods.add(ElectricBillPeriod(code = it.id, fromDate = it.inicio, toDate = it.fin, active = it.activo, meterCode = it.medidor.id))
                else
                    newPeriods.add(ElectricBillPeriod(code = it.id, fromDate = it.inicio, toDate = Date(), active = it.activo, meterCode = it.medidor.id))
            }
        }
        dao.savePeriods(newPeriods)

        // READINGS
        val oldReadins = realm.where(Lectura::class.java).findAll().sort("fecha_lectura", Sort.ASCENDING)
        val newReadings:MutableList<ElectricReading> = ArrayList()
        oldReadins?.forEach {
            //Log.e("EDER", it.fecha_lectura.formatDate(context))
            if(!readingExist(it.id)) {
                newReadings.add(ElectricReading(
                        code = it.id, comments = if(it.observacion.isNullOrEmpty())"" else it.observacion, periodCode = it.periodo.id,
                        meterCode = it.medidor.id, readingDate = it.fecha_lectura, readingValue = it.lectura,
                        kwConsumption = it.consumo, kwAggConsumption = it.consumo_acumulado, kwAvgConsumption = (it.consumo_promedio / 24),
                        consumptionHours = it.dias_periodo * 24
                ))
            }
        }
        dao.saveReadings(newReadings)
        val periods = dao.getPeriodList()
        periods.forEach {
            it.totalKw = dao.getTotalPeriodKw(it.code)
            dao.updatePeriod(it)
        }
        realm.close()
    }

    fun migrationDataAvailable():Boolean{
        val realm = Realm.getDefaultInstance()
        val oldMeters = realm.where(Medidor::class.java).findAll()
        return oldMeters.isNotEmpty()
    }
}