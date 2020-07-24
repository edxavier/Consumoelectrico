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
    suspend fun getOverlappingPrice(min:Int, max:Int) = withContext(Dispatchers.IO){ dao.getOverlappingPrice(min, max) }

    suspend fun getLastTwoElectricReadings(periodCode: String) = withContext(Dispatchers.IO){ dao.getLastTwoPeriodElectricReadings(periodCode) }
    suspend fun getLastElectricPeriod(meterCode: String) = withContext(Dispatchers.IO){ dao.getLastElectricPeriod(meterCode) }

    suspend fun validatedReadingValue(readingDate: Date, readingValue:Float):Boolean = withContext(Dispatchers.IO){
        val future = dao.countInvalidFutureReadings(readingDate, readingValue)
        val past = dao.countInvalidPastReadings(readingDate, readingValue)
        return@withContext future <= 0 && past <=0
    }

    suspend fun savedReading(reading: ElectricReading, meterCode: String) = withContext(Dispatchers.IO){
        val period = dao.getLastElectricPeriod(meterCode)
        if (period!=null){
            reading.periodCode = period.code
            reading.meterCode = meterCode
            val lastPeriodReadings = dao.getLastTwoPeriodElectricReadings(period.code)
            if (lastPeriodReadings.isNotEmpty()){
                val previous = lastPeriodReadings.first()
                Log.e("EDER", "Existe almenos 1 lectura")
                computeReading(reading, previous, period, false)
            }else{
                Log.e("EDER", "Periodo sin lecturaas")
                //Si el periodo no tiene lecturas, cargar las ultimas lecturas del periodo anterior
                val previousReadings = dao.getLastTwoMeterElectricReadings(meterCode)
                if(previousReadings.isNotEmpty()){
                    val previous = lastPeriodReadings.first()
                    computeReading(reading, previous, period, true)
                }
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
        }else
            Log.e("EDER", "ERROR AL CREAR PRIMER PERIODO")
    }

    private fun computeReading(current:ElectricReading, previous:ElectricReading,
                               period:ElectricBillPeriod, prevPeriodReading:Boolean){
        val startDate = LocalDate(period.fromDate)
        val endDate = LocalDate(current.readingDate)
        //inicializar  variable p, para calcular las horas desde que inicio el periodo hasta la fecha de la lectura actual
        val totalHours = Period(startDate, endDate, PeriodType.hours())
        val previousHours = Period(LocalDate(previous.readingDate), endDate, PeriodType.hours())
        current.kwConsumption = current.readingValue - previous.readingValue
        current.consumptionHours = totalHours.hours.toFloat()
        current.consumptionPreviousHours = previousHours.hours.toFloat()
        if (prevPeriodReading)
            current.kwAggConsumption = current.kwConsumption
        else
            current.kwAggConsumption = current.kwConsumption + previous.kwAggConsumption
        current.kwAvgConsumption = current.kwAggConsumption / current.consumptionHours
        dao.saveReading(current)
    }

}