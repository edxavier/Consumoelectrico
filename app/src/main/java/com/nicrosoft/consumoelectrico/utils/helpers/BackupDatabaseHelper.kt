package com.nicrosoft.consumoelectrico.utils.helpers

import android.content.Context
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.nicrosoft.consumoelectrico.R
import com.nicrosoft.consumoelectrico.data.ExpenseDetail
import com.nicrosoft.consumoelectrico.data.LineChartDataSets
import com.nicrosoft.consumoelectrico.data.daos.BackupDAO
import com.nicrosoft.consumoelectrico.data.daos.ElectricMeterDAO
import com.nicrosoft.consumoelectrico.data.entities.ElectricBillPeriod
import com.nicrosoft.consumoelectrico.data.entities.ElectricMeter
import com.nicrosoft.consumoelectrico.data.entities.ElectricReading
import com.nicrosoft.consumoelectrico.data.entities.PriceRange
import com.nicrosoft.consumoelectrico.utils.getConsumptionProjection
import com.nicrosoft.consumoelectrico.utils.hoursSinceDate
import com.nicrosoft.consumoelectrico.utils.charts.setupAppStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.time.ExperimentalTime

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
            if(periodExist(it.code))
                tempList.remove(it)
        }
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

}