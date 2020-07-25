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
import org.joda.time.LocalDate
import org.joda.time.Period
import org.joda.time.PeriodType
import java.util.*

class ElectricViewModel(val context: Context, private val dao:ElectricMeterDAO) : ViewModel() {

    var meter = MutableLiveData<ElectricMeter>()
    fun selectedMeter(_meter: ElectricMeter) { meter.value = _meter }


    fun getElectricMeterList() = dao.getMeters()
    fun getPeriodMetersReadings(periodCode:String) = dao.getPeriodMetersReadings(periodCode)
    fun getAllMeterReadings(meterCode:String) = dao.getAllMeterReadings(meterCode)
    fun getPriceList(meterCode:String) = dao.getPriceRanges(meterCode)

    suspend fun saveElectricMeter(meter:ElectricMeter) = withContext(Dispatchers.IO){ dao.saveElectricMeter(meter) }
    suspend fun savePrice(price:PriceRange) = withContext(Dispatchers.IO){ dao.savePriceRage(price) }

    suspend fun deleteElectricMeter(meter:ElectricMeter) = withContext(Dispatchers.IO){ dao.deleteElectricMeter(meter) }
    suspend fun deletePriceRange(price:PriceRange) = withContext(Dispatchers.IO){ dao.deletePriceRage(price) }

    suspend fun updateElectricMeter(meter:ElectricMeter) = withContext(Dispatchers.IO){ dao.updateElectricMeter(meter) }
    suspend fun updatePriceRange(price:PriceRange) = withContext(Dispatchers.IO){ dao.updatePriceRage(price) }
    suspend fun getOverlappingPrice(min:Int, max:Int, meterCode: String) = withContext(Dispatchers.IO){ dao.getOverlappingPrice(min, max, meterCode) }

    suspend fun getLastPeriodReadings(periodCode: String) = withContext(Dispatchers.IO){ dao.getLastPeriodReading(periodCode) }
    suspend fun getLastElectricPeriod(meterCode: String) = withContext(Dispatchers.IO){ dao.getLastElectricPeriod(meterCode) }

    suspend fun validatedReadingValue(readingDate: Date, readingValue:Float, meterCode: String):Boolean = withContext(Dispatchers.IO){
        val future = dao.countInvalidFutureReadings(readingDate, readingValue, meterCode)
        val past = dao.countInvalidPastReadings(readingDate, readingValue, meterCode)
        return@withContext future <= 0 && past <=0
    }

    suspend fun savedReading(reading: ElectricReading, meterCode: String, terminatePeriod:Boolean) = withContext(Dispatchers.IO){
        val period = dao.getLastElectricPeriod(meterCode)
        if (period!=null){
            reading.periodCode = period.code
            reading.meterCode = meterCode
            val totalReadings = dao.getTotalPeriodReading(period.code)
            if (totalReadings>0){
                Log.e("EDER", "Existe almenos 1 lectura")
                val previous = dao.getPreviousReading(period.code, reading.readingDate)
                val next = dao.getNextReading(period.code, reading.readingDate)
                if(previous!=null) {
                    Log.e("EDER", "HAY LECTURA PREVIA")
                    computeReading(reading, previous, next, period, false)
                }else {
                    //No se encontro lecturas anterirores a la neuva en este periodo, esta pasa a ser la primera
                    //Cargar ultima lectura del periodo aanterior
                    Log.e("EDER", "No HAY LECTURA PREVIA")
                    val previousReading = dao.getLastMeterReading(meterCode, reading.readingDate)
                    computeReading(reading, previousReading!!, next, period, true)
                }

            }else{
                //Si el periodo no tiene lecturas, cargar las ultimas lecturas del periodo anterior
                val previousReading = dao.getLastMeterReading(meterCode, reading.readingDate)
                val nextReading = null
                if(previousReading!=null)
                    computeReading(reading, previousReading, nextReading,  period, true)
            }
        }else{
            //Si es el primer periodo crear de cero
            createFirstPeriod(reading,meterCode)
        }
    }

    private fun createFirstPeriod(reading: ElectricReading, meterCode: String){
        val newPeriod = ElectricBillPeriod(fromDate = reading.readingDate, meterCode = meterCode, toDate = reading.readingDate)
        dao.savePeriod(newPeriod)
        val period = dao.getLastElectricPeriod(meterCode)
        if(period!=null) {
            reading.periodCode = period.code
            reading.meterCode = meterCode
            dao.saveReading(reading)
            Log.e("EDER", "Primera lectura y periodo creado")
        }
    }

    private fun computeReading(current:ElectricReading, previous:ElectricReading,
                               next:ElectricReading?, period:ElectricBillPeriod, isFirstPeriodReading:Boolean){
        val startDate = LocalDate(period.fromDate)
        val endDate = LocalDate(current.readingDate)
        //inicializar  variable p, para calcular las horas desde que inicio el periodo hasta la fecha de la lectura actual
        val totalHours = Period(startDate, endDate, PeriodType.hours())
        var previousHours = Period(LocalDate(previous.readingDate), endDate, PeriodType.hours())
        current.kwConsumption = current.readingValue - previous.readingValue
        current.consumptionHours = totalHours.hours.toFloat()
        current.consumptionPreviousHours = previousHours.hours.toFloat()
        if (isFirstPeriodReading)
            current.kwAggConsumption = current.kwConsumption
        else
            current.kwAggConsumption = current.kwConsumption + previous.kwAggConsumption
        current.kwAvgConsumption = current.kwAggConsumption / current.consumptionHours

        if(next!=null){
            Log.e("EDER", "HAY LECTURAS POSTERIORES")
            previousHours = Period(LocalDate(current.readingDate), LocalDate(next.readingDate), PeriodType.hours())
            next.kwConsumption = next.readingValue - current.readingValue
            next.consumptionPreviousHours = previousHours.hours.toFloat()
            next.kwAggConsumption = next.kwConsumption + current.kwAggConsumption
            next.kwAvgConsumption = next.kwAggConsumption / next.consumptionHours
        }

        dao.saveReading(current)
    }

}