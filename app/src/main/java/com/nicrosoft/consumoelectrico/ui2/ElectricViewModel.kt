package com.nicrosoft.consumoelectrico.ui2

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.nicrosoft.consumoelectrico.data.daos.ElectricMeterDAO
import com.nicrosoft.consumoelectrico.data.entities.ElectricBillPeriod
import com.nicrosoft.consumoelectrico.data.entities.ElectricMeter
import com.nicrosoft.consumoelectrico.data.entities.ElectricReading
import com.nicrosoft.consumoelectrico.data.entities.PriceRange
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

class ElectricViewModel(val context: Context, private val dao:ElectricMeterDAO) : ViewModel() {

    var meter = MutableLiveData<ElectricMeter>()
    fun selectedMeter(_meter: ElectricMeter) { meter.value = _meter }


    fun getElectricMeterList() = dao.getMeters()
    fun getPriceList(meter_id:Int) = dao.getPriceRanges(meter_id)

    suspend fun saveElectricMeter(meter:ElectricMeter) = withContext(Dispatchers.IO){ dao.saveElectricMeter(meter) }
    suspend fun savePrice(price:PriceRange) = withContext(Dispatchers.IO){ dao.savePriceRage(price) }

    suspend fun deleteElectricMeter(meter:ElectricMeter) = withContext(Dispatchers.IO){ dao.deleteElectricMeter(meter) }
    suspend fun deletePriceRange(price:PriceRange) = withContext(Dispatchers.IO){ dao.deletePriceRage(price) }

    suspend fun updateElectricMeter(meter:ElectricMeter) = withContext(Dispatchers.IO){ dao.updateElectricMeter(meter) }
    suspend fun updatePriceRange(price:PriceRange) = withContext(Dispatchers.IO){ dao.updatePriceRage(price) }
    suspend fun getOverlappingPrice(min:Int, max:Int) = withContext(Dispatchers.IO){ dao.getOverlappingPrice(min, max) }

    suspend fun getLastTwoElectricReadings(periodId:Int) = withContext(Dispatchers.IO){ dao.getLastTwoElectricReadings(periodId) }
    suspend fun getLastElectricPeriod(meterId:Int) = withContext(Dispatchers.IO){ dao.getLastElectricPeriod(meterId) }

    suspend fun validatedReadingValue(readingDate: Date, readingValue:Float):Boolean = withContext(Dispatchers.IO){
        val invalids = dao.countInvalidReadings(readingDate, readingValue)
        return@withContext invalids <= 0
    }

    suspend fun savedReading(reading: ElectricReading, meter_id: Int) = withContext(Dispatchers.IO){
        var period = dao.getLastElectricPeriod(meter_id)
        if (period!=null){
            reading.periodId = period.id
            val lastPeriodReadings = dao.getLastTwoElectricReadings(period.id!!)
            if (lastPeriodReadings.isNotEmpty()){
                val last = lastPeriodReadings.first()
            }
        }else{
            //Si es el primer periodo crear de cero
            createFirstPeriod(reading,meter_id)
        }
    }

    private fun createFirstPeriod(reading: ElectricReading, meter_id: Int){
        val newPeriod = ElectricBillPeriod(fromDate = reading.readingDate, meterId = meter_id)
        dao.savePeriod(newPeriod)
        val period = dao.getLastElectricPeriod(meter_id)
        if(period!=null) {
            reading.periodId = period.id
            dao.saveReading(reading)
        }else
            Log.e("EDER", "ERROR AL CREAR PRIMER PERIODO")
    }
}