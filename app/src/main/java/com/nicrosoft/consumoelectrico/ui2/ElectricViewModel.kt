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
    fun getMeterPeriods(meterCode: String) = dao.getMeterPeriods(meterCode)
    fun getPeriodMetersReadings(periodCode:String) = dao.getPeriodMetersReadings(periodCode)
    fun getAllMeterReadings(meterCode:String) = dao.getAllMeterReadings(meterCode)
    suspend fun getFirstMeterReading(meterCode:String) = withContext(Dispatchers.IO){ dao.getFirstMeterReading(meterCode) }
    fun getPriceList(meterCode:String) = dao.getPriceRanges(meterCode)

    suspend fun getMeter(meterCode: String) = withContext(Dispatchers.IO){ dao.getMeter(meterCode) }
    suspend fun saveElectricMeter(meter:ElectricMeter) = withContext(Dispatchers.IO){ dao.saveElectricMeter(meter) }
    suspend fun savePrice(price:PriceRange) = withContext(Dispatchers.IO){ dao.savePriceRage(price) }

    suspend fun deleteElectricMeter(meter:ElectricMeter) = withContext(Dispatchers.IO){ dao.deleteElectricMeter(meter) }
    suspend fun deletePriceRange(price:PriceRange) = withContext(Dispatchers.IO){ dao.deletePriceRage(price) }

    suspend fun updateElectricMeter(meter:ElectricMeter) = withContext(Dispatchers.IO){ dao.updateElectricMeter(meter) }
    suspend fun updatePriceRange(price:PriceRange) = withContext(Dispatchers.IO){ dao.updatePriceRage(price) }
    suspend fun getOverlappingPrice(min:Int, max:Int, meterCode: String) = withContext(Dispatchers.IO){ dao.getOverlappingPrice(min, max, meterCode) }

    suspend fun getLastPeriodReading(periodCode: String) = withContext(Dispatchers.IO){ dao.getLastPeriodReading(periodCode) }
    suspend fun getFirstPeriodReading(periodCode: String) = withContext(Dispatchers.IO){ dao.getFirstPeriodReading(periodCode) }
    suspend fun getLastPeriod(meterCode: String) = withContext(Dispatchers.IO){ dao.getLastElectricPeriod(meterCode) }

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
            period.totalKw = dao.getTotalPeriodKw(period.code)
            val lr = dao.getLastPeriodReading(period.code)
            if(lr!=null)
                period.toDate = lr.readingDate
            dao.updatePeriod(period)
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
    suspend fun terminatePeriod(current:ElectricReading, period:ElectricBillPeriod, meterCode: String) = withContext(Dispatchers.IO) {
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


    @ExperimentalTime
    suspend fun updateReadingValue(reading:ElectricReading) = withContext(Dispatchers.IO){
        val previous = dao.getPreviousReading(reading.periodCode!!, reading.readingDate)
        val next = dao.getNextReading(reading.periodCode!!, reading.readingDate)
        var previousPeriodLastReading:ElectricReading? = null
        if (previous==null) {
            previousPeriodLastReading = dao.getLastMeterReading(reading.meterCode!!, reading.readingDate)
        }
        recomputeAndUpdate(previous, reading, previousPeriodLastReading)
        recomputeAndUpdate(reading, next, previousPeriodLastReading)
        //dao.updateElectricReading(reading)
    }


    @ExperimentalTime
    private fun recomputeAndUpdate(previous: ElectricReading?, next: ElectricReading?, prevPeriodLastReading:ElectricReading?) {
        //Si hay un proximo recalcular los valores
        //si no hay proximo no es necesario hacer ningun calculo, los valores anteriores no dependen de lo que se esta eliminando
        //por tanto si solo hay una lectura y es la primera no sera necesario nada mas que eliminarla
        next?.let {
            if(previous!=null){
                //Solo se recalcula el consumo entre las lecturas y las horas consumo entre ellas, lo demas no se ve afectado ya que se calcula respecto al inicio periodo
                it.kwConsumption = it.readingValue - previous.readingValue
                it.consumptionPreviousHours = it.readingDate.hoursSinceDate(previous.readingDate).toFloat()
                it.kwAggConsumption = it.kwConsumption + previous.kwAggConsumption
                it.kwAvgConsumption = it.kwAggConsumption / it.consumptionHours
            }else{
                //No hay lectura previa asi que cargar la ultima lectura del periodo anterior
                if(prevPeriodLastReading!=null){
                    next.kwConsumption = next.readingValue - prevPeriodLastReading.readingValue
                    next.consumptionPreviousHours = next.readingDate.hoursSinceDate(prevPeriodLastReading.readingDate).toFloat()
                    next.consumptionHours = next.consumptionPreviousHours
                    next.kwAggConsumption = next.kwConsumption
                    next.kwAvgConsumption = next.kwAggConsumption / next.consumptionHours
                }else{
                    next.kwConsumption = 0f
                    next.consumptionPreviousHours = 0f
                    next.consumptionHours = 0f
                    next.kwAggConsumption = 0f
                    next.kwAvgConsumption = 0f
                    recomputeLaterReadings(next)
                }
            }
            dao.updateElectricReading(next)
        }
    }

    @ExperimentalTime
    suspend fun deleteElectricReading(reading:ElectricReading) = withContext(Dispatchers.IO){
        Log.e("EDER", "DELETED ${reading.readingValue}")
        val previous = dao.getPreviousReading(reading.periodCode!!, reading.readingDate)
        val next = dao.getNextReading(reading.periodCode!!, reading.readingDate)
        var previousPeriodLastReading:ElectricReading? = null
        if (previous==null)
            previousPeriodLastReading = dao.getLastMeterReading(reading.meterCode!!, reading.readingDate)
        recomputeAndUpdate(previous, next, previousPeriodLastReading)
        dao.deleteElectricReading(reading)
        Log.e("EDER", "DELETED ${reading.readingValue}")
    }

    @ExperimentalTime
    private fun recomputeLaterReadings(reading: ElectricReading){
        val laterReadings = dao.getReadingsAfter(reading.periodCode!!, reading.readingDate)
        laterReadings.forEachIndexed { index, electricReading ->
            Log.e("EDER", "RECALCULANDO lecturas")
            //Recalcular lecturas despues de la lectura pasada, que solo seria en caso de que esta sea la primer lectura de la hsitoria del medidor
            if(index>0){
                electricReading.consumptionHours = electricReading.readingDate.hoursSinceDate(reading.readingDate).toFloat()
                //electricReading.consumptionPreviousHours = electricReading.readingDate.hoursSinceDate(newPeriod.fromDate).toFloat()
                electricReading.kwAggConsumption = electricReading.kwConsumption
                electricReading.kwAvgConsumption = electricReading.kwAggConsumption / electricReading.consumptionHours
            }else{
                val prev = laterReadings[index-1]
                electricReading.consumptionHours = electricReading.readingDate.hoursSinceDate(reading.readingDate).toFloat()
                //electricReading.consumptionPreviousHours = electricReading.readingDate.hoursSinceDate(newPeriod.fromDate).toFloat()
                electricReading.kwAggConsumption = electricReading.kwConsumption + prev.kwAggConsumption
                electricReading.kwAvgConsumption = electricReading.kwAggConsumption / electricReading.consumptionHours
            }
            dao.updateElectricReading(electricReading)
        }
    }
}