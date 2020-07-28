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
import com.nicrosoft.consumoelectrico.utils.formatDate
import com.nicrosoft.consumoelectrico.utils.hoursSinceDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.joda.time.LocalDate
import org.joda.time.Period
import org.joda.time.PeriodType
import java.util.*
import kotlin.time.ExperimentalTime

class ElectricViewModel(val context: Context, private val dao:ElectricMeterDAO) : ViewModel() {

    var meter = MutableLiveData<ElectricMeter>()
    fun selectedMeter(_meter: ElectricMeter) { meter.value = _meter }


    fun getElectricMeterList() = dao.getMeters()
    fun getPeriodMetersReadings(periodCode:String) = dao.getPeriodMetersReadings(periodCode)
    fun getAllMeterReadings(meterCode:String) = dao.getAllMeterReadings(meterCode)
    fun getPriceList(meterCode:String) = dao.getPriceRanges(meterCode)

    suspend fun getMeter(meterCode: String) = withContext(Dispatchers.IO){ dao.getMeter(meterCode) }
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

    @ExperimentalTime
    suspend fun savedReading(reading: ElectricReading, meterCode: String, terminatePeriod:Boolean) = withContext(Dispatchers.IO){
        val period = dao.getLastElectricPeriod(meterCode)
        if (period!=null){
            reading.periodCode = period.code
            reading.meterCode = meterCode
            val totalReadings = dao.getTotalPeriodReading(period.code)
            if (totalReadings>0){
                val previous = dao.getPreviousReading(period.code, reading.readingDate)
                val next = dao.getNextReading(period.code, reading.readingDate)
                if(previous!=null) {
                    computeReading(reading, previous, next, period, false)
                    period.totalKw = dao.getTotalPeriodKw(period.code)
                    dao.updatePeriod(period)
                    if(terminatePeriod) {terminatePeriod(reading, period, meterCode)}else{}
                }else {
                    //No se encontro lecturas anterirores a la neuva en este periodo, esta pasa a ser la primera
                    //Cargar ultima lectura del periodo aanterior
                    val previousReading = dao.getLastMeterReading(meterCode, reading.readingDate)
                    computeReading(reading, previousReading!!, next, period, true)
                    period.totalKw = dao.getTotalPeriodKw(period.code)
                    dao.updatePeriod(period)
                }
            }else{
                //Si el periodo no tiene lecturas, cargar las ultimas lecturas del periodo anterior
                val previousReading = dao.getLastMeterReading(meterCode, reading.readingDate)
                val nextReading = null
                if(previousReading!=null){
                    computeReading(reading, previousReading, nextReading,  period, true)}else{}

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

    @ExperimentalTime
    private fun computeReading(current:ElectricReading, previous:ElectricReading,
                               next:ElectricReading?, period:ElectricBillPeriod, isFirstPeriodReading:Boolean){
        //inicializar  variable p, para calcular las horas desde que inicio el periodo hasta la fecha de la lectura actual
        val totalHours = current.readingDate.hoursSinceDate(period.fromDate)
        var previousHours = current.readingDate.hoursSinceDate(previous.readingDate)

        current.kwConsumption = current.readingValue - previous.readingValue
        current.consumptionHours = totalHours.toFloat()
        current.consumptionPreviousHours = previousHours.toFloat()
        if (isFirstPeriodReading)
            current.kwAggConsumption = current.kwConsumption
        else
            current.kwAggConsumption = current.kwConsumption + previous.kwAggConsumption
        current.kwAvgConsumption = current.kwAggConsumption / current.consumptionHours

        next?.let {
            Log.e("EDER", "HAY LECTURAS POSTERIORES")
            previousHours = next.readingDate.hoursSinceDate(current.readingDate)
            next.kwConsumption = next.readingValue - current.readingValue
            next.consumptionPreviousHours = previousHours.toFloat()
            next.kwAggConsumption = next.kwConsumption + current.kwAggConsumption
            next.kwAvgConsumption = next.kwAggConsumption / next.consumptionHours
            dao.updateElectricReading(next)
        }
        dao.saveReading(current)
    }

    @ExperimentalTime
    fun terminatePeriod(current:ElectricReading, period:ElectricBillPeriod, meterCode: String){
        //Crear nuevo periodo, cerrar el actual y reasignar toda lectura posterior al nuevo periodo
        val newPeriod = ElectricBillPeriod(fromDate = current.readingDate, meterCode = meterCode, toDate = current.readingDate)
        period.toDate = current.readingDate
        period.active = false
        period.totalKw = dao.getTotalPeriodKw(period.code)
        dao.updatePeriod(period)
        dao.savePeriod(newPeriod)
        val laterReadings = dao.getReadingsAfter(period.code, current.readingDate)
        laterReadings.forEachIndexed { index, electricReading ->
            Log.e("EDER", "Reasignado lecturas")
            //Asignar lecturas al nuevo periodo
            electricReading.periodCode = newPeriod.code
            if(index==0){
                electricReading.consumptionHours = electricReading.readingDate.hoursSinceDate(newPeriod.fromDate).toFloat()
                //electricReading.consumptionPreviousHours = electricReading.readingDate.hoursSinceDate(newPeriod.fromDate).toFloat()
                electricReading.kwAggConsumption = electricReading.kwConsumption
                electricReading.kwAvgConsumption = electricReading.kwAggConsumption / electricReading.consumptionHours
            }else{
                val prev = laterReadings[index-1]
                electricReading.consumptionHours = electricReading.readingDate.hoursSinceDate(newPeriod.fromDate).toFloat()
                //electricReading.consumptionPreviousHours = electricReading.readingDate.hoursSinceDate(newPeriod.fromDate).toFloat()
                electricReading.kwAggConsumption = electricReading.kwConsumption + prev.kwAggConsumption
                electricReading.kwAvgConsumption = electricReading.kwAggConsumption / electricReading.consumptionHours
            }
            dao.updateElectricReading(electricReading)
        }
    }
}