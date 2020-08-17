package com.nicrosoft.consumoelectrico.viewmodels

import android.content.Context
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

class ElectricViewModel(val context: Context, private val dao:ElectricMeterDAO) : ViewModel() {

    fun getDao() = dao
    var meter = MutableLiveData<ElectricMeter>()
    fun selectedMeter(_meter: ElectricMeter) { meter.value = _meter }


    fun getElectricMeterList() = dao.getMeters()
    fun getMeterPeriods(meterCode: String) = dao.getMeterPeriods(meterCode)
    fun getAllMeterReadings(meterCode:String) = dao.getAllMeterReadings(meterCode)

    suspend fun getMeterAllPeriods(meterCode: String) = withContext(Dispatchers.IO){ dao.getMeterAllPeriods(meterCode) }
    suspend fun getPeriodMetersReadings(periodCode:String) = withContext(Dispatchers.IO){ dao.getPeriodMetersReadings(periodCode) }
    suspend fun getFirstMeterReading(meterCode:String) = withContext(Dispatchers.IO){ dao.getFirstMeterReading(meterCode) }
    suspend fun getLastPriceRange(meterCode:String) = withContext(Dispatchers.IO){ dao.getLastPriceRange(meterCode) }
    suspend fun getNextPriceRange(meterCode:String, priceFrom:Int) = withContext(Dispatchers.IO){ dao.getNextPriceRange(meterCode, priceFrom) }
    suspend fun getPreviousPriceRange(meterCode:String, priceFrom:Int) = withContext(Dispatchers.IO){ dao.getPreviousPriceRange(meterCode, priceFrom) }

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
                    updatePeriodTotals(period.code)
                    if(terminatePeriod) {terminatePeriod(reading, period, meterCode)}else{}
                }else {
                    //No se encontro lecturas anterirores a la neuva en este periodo, esta pasa a ser la primera
                    //Cargar ultima lectura del periodo aanterior
                    val previousReading = dao.getLastMeterReading(meterCode, reading.readingDate)
                    computeReading(reading, previousReading!!, next, period, true)
                    updatePeriodTotals(period.code)
                }
            }else{
                //Si el periodo no tiene lecturas, cargar las ultimas lecturas del periodo anterior
                val previousReading = dao.getLastMeterReading(meterCode, reading.readingDate)
                val nextReading = null
                if(previousReading!=null){
                    computeReading(reading, previousReading, nextReading,  period, true)}else{}

            }
            val lr = dao.getLastPeriodReading(period.code)
            if(lr!=null)
                period.toDate = lr.readingDate
            dao.updatePeriod(period)
            updatePeriodTotals(period.code)
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
    private suspend fun computeReading(current:ElectricReading, previous:ElectricReading,
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
            //Log.e("EDER", "HAY LECTURAS POSTERIORES")
            previousHours = next.readingDate.hoursSinceDate(current.readingDate)
            next.kwConsumption = next.readingValue - current.readingValue
            next.consumptionPreviousHours = previousHours.toFloat()
            next.kwAggConsumption = next.kwConsumption + current.kwAggConsumption
            next.kwAvgConsumption = next.kwAggConsumption / next.consumptionHours
            dao.updateElectricReading(next)
        }
        dao.saveReading(current)
        //updatePeriodTotals(period.code)
    }

    @ExperimentalTime
    suspend fun terminatePeriod(current:ElectricReading, period:ElectricBillPeriod, meterCode: String) = withContext(Dispatchers.IO) {
        //Crear nuevo periodo, cerrar el actual y reasignar toda lectura posterior al nuevo periodo
        var newPeriod = ElectricBillPeriod(fromDate = current.readingDate, meterCode = meterCode, toDate = current.readingDate)
        period.toDate = current.readingDate
        period.active = false
        dao.updatePeriod(period)
        updatePeriodTotals(period.code)
        dao.savePeriod(newPeriod)
        newPeriod = dao.getPeriod(newPeriod.code)
        val laterReadings = dao.getReadingsAfter(period.code, current.readingDate)
        laterReadings.forEachIndexed { index, electricReading ->
            //Log.e("EDER", "Reasignado lecturas")
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
        if(laterReadings.isNotEmpty()){
            newPeriod.toDate = laterReadings.last().readingDate
            dao.updatePeriod(newPeriod)
        }
        updatePeriodTotals(newPeriod.code)
    }


    @ExperimentalTime
    suspend fun updateReadingValue(reading:ElectricReading) = withContext(Dispatchers.IO){
        val pCode = reading.periodCode
        val previous = dao.getPreviousReading(reading.periodCode!!, reading.readingDate)
        val next = dao.getNextReading(reading.periodCode!!, reading.readingDate)
        var previousPeriodLastReading:ElectricReading? = null
        if (previous==null) {
            previousPeriodLastReading = dao.getLastMeterReading(reading.meterCode!!, reading.readingDate)
        }
        recomputeAndUpdate(previous, reading, previousPeriodLastReading)
        recomputeAndUpdate(reading, next, previousPeriodLastReading)
        updatePeriodTotals(pCode!!)
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
        //Log.e("EDER", "DELETED ${reading.readingValue}")
        val pCode = reading.periodCode
        val previous = dao.getPreviousReading(reading.periodCode!!, reading.readingDate)
        val next = dao.getNextReading(reading.periodCode!!, reading.readingDate)
        var previousPeriodLastReading:ElectricReading? = null
        if (previous==null)
            previousPeriodLastReading = dao.getLastMeterReading(reading.meterCode!!, reading.readingDate)
        recomputeAndUpdate(previous, next, previousPeriodLastReading)
        dao.deleteElectricReading(reading)
        updatePeriodTotals(pCode!!)
        //Log.e("EDER", "DELETED ${reading.readingValue}")
    }

    private suspend fun updatePeriodTotals(periodCode: String){
        val period = dao.getPeriod(periodCode)
        period.totalKw = dao.getTotalPeriodKw(period.code)
        period.totalBill = calculateEnergyCosts(period.totalKw, meter.value!!).total
        val costUpdated = dao.updatePeriod(period)
        //Log.e("EDER_COSTUPDATED", costUpdated.toString())
    }

    @ExperimentalTime
    private fun recomputeLaterReadings(reading: ElectricReading){
        val laterReadings = dao.getReadingsAfter(reading.periodCode!!, reading.readingDate)
        laterReadings.forEachIndexed { index, electricReading ->
            //Log.e("EDER", "RECALCULANDO lecturas")
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

    suspend fun calculateEnergyCosts(totalKWh:Float, meter: ElectricMeter):ExpenseDetail = withContext(Dispatchers.IO){
        val eDetails = ExpenseDetail()
        val prices = dao.getPricesList(meter.code)
        var energyExp = if(prices.isNotEmpty()){
            calculatePricesExpenses(totalKWh, prices, meter)
        }else
            totalKWh * meter.kwPrice
        eDetails.energy = energyExp
        val discount = if(meter.loseDiscount){
            if(totalKWh > meter.maxKwLimit)
                0f
            else
                energyExp * (meter.kwDiscount/100)
        }else
            energyExp * (meter.kwDiscount/100)
        eDetails.discount = discount
        energyExp -= discount
        val taxes = energyExp * (meter.taxes/100)
        eDetails.taxes = taxes
        energyExp += (taxes + meter.fixedPrices)
        eDetails.fixed = meter.fixedPrices
        eDetails.total = energyExp
        return@withContext eDetails
    }

    private fun calculatePricesExpenses(totalKWh: Float, prices:List<PriceRange>, meter: ElectricMeter):Float{
        var expenses = 0f
        var energy = totalKWh
        //Log.e("EDER INICIO", "Exp: $expenses ---- energy: $energy")
        prices.forEach { p ->
            if(p.toKw<totalKWh){
                val diff = if(p.fromKw>0)
                    p.toKw - (p.fromKw-1)
                else
                    p.toKw - p.fromKw
                expenses += diff * p.price
                energy-= diff
                //Log.e("EDER", "total $totalKWh esta sobre el rango ${p.fromKw} - ${p.toKw}.... Diff: $diff Rest: $energy Gasto: $expenses")
            }else if(totalKWh>p.fromKw && totalKWh<p.toKw){
                val diff = if(p.fromKw>0)
                    totalKWh - (p.fromKw-1)
                else
                    totalKWh - p.fromKw
                expenses += diff * p.price
                energy-= diff
                //Log.e("EDER", "total $totalKWh esta dentro del rango ${p.fromKw} - ${p.toKw}.... Diff: $diff Rest: $energy Gasto: $expenses")
            }
        }
        if(energy>0)
            expenses += energy * meter.kwPrice
        //Log.e("EDER", "Rest: ${energy-energy} Gasto: $expenses")
        //Log.e("EDER FIN", "--------------------------------------------------------")
        return expenses
    }

    suspend fun getLineChartData(period: ElectricBillPeriod): LineChartDataSets = withContext(Dispatchers.IO){
        val dSets = LineChartDataSets()
        val readings = dao.getPeriodReadings(period.code)

        dSets.consumptionDs = getConsumptionChartDataSets(readings)
        dSets.dailyAvgDs = getAvgConsumptionChartDataSets(readings)
        dSets.costPerDayDs = getCostPerDayChartDataSets(readings)
        dSets.costPerKwDs = getCostPerKwhChartDataSets(readings)
        dSets.periodDs = getPeriodDataSets()
        return@withContext dSets
    }

    private fun getConsumptionProjectionDataSet(reading: ElectricReading, color:Int): LineDataSet{
        val entries: MutableList<Entry> = ArrayList()
        if(reading.kwAggConsumption < meter.value!!.maxKwLimit){
            val projection = reading.getConsumptionProjection(meter.value!!)
            entries.add(Entry(reading.consumptionHours, reading.kwAggConsumption))
            entries.add(Entry((meter.value!!.periodLength*24).toFloat(), projection))
        }
        val dataSet = LineDataSet(entries, "Proyeccion")
        dataSet.setupAppStyle(context)
        dataSet.enableDashedLine(15f, 15f, 0f)
        dataSet.color = color
        return dataSet
    }

    private fun getConsumptionChartDataSets(readings:List<ElectricReading>): LineData {
        val entries: MutableList<Entry> = ArrayList()
        readings.forEach { r -> entries.add(Entry(r.consumptionHours, r.kwAggConsumption)) }
        val dataSet = LineDataSet(entries, context.getString(R.string.chart_legend_accumulated))
        dataSet.setupAppStyle(context)
        val dataSets: MutableList<ILineDataSet> = ArrayList()
        dataSets.add(dataSet)
        if(readings.isNotEmpty()){
            val projectionDs = getConsumptionProjectionDataSet(readings.last(),
                    ContextCompat.getColor(context, R.color.md_blue_grey_400))
            dataSets.add(projectionDs)
        }
        return LineData(dataSets)
    }
    private fun getAvgConsumptionChartDataSets(readings:List<ElectricReading>): LineData {
        val dailyEntries: MutableList<Entry> = ArrayList()
        val hourlyEntries: MutableList<Entry> = ArrayList()
        readings.forEach { r ->
            dailyEntries.add(Entry(r.consumptionHours, (r.kwAvgConsumption*24)))
            hourlyEntries.add(Entry(r.consumptionHours, r.kwAvgConsumption))
        }
        val dailyDataSet = LineDataSet(dailyEntries, context.getString(R.string.chart_legend_avg))
        val hourlyDataSet = LineDataSet(hourlyEntries, "Promedio por hora")
        dailyDataSet.setupAppStyle(context)
        hourlyDataSet.setupAppStyle(context)
        hourlyDataSet.color = ContextCompat.getColor(context, R.color.md_teal_500)
        val dataSets: MutableList<ILineDataSet> = ArrayList()
        dataSets.add(dailyDataSet)
        dataSets.add(hourlyDataSet)
        return LineData(dataSets)
    }

    private suspend fun getCostPerDayChartDataSets(readings:List<ElectricReading>): LineData {
        val entries: MutableList<Entry> = ArrayList()
        readings.forEach { r ->
            val cost = calculateEnergyCosts(r.kwAggConsumption, meter.value!!)
            val total = if (cost.energy > 0)
                cost.total
            else
                0f
            entries.add(Entry( r.consumptionHours, total))
        }
        val dataSet = LineDataSet(entries, "Gasto vs dias")
        dataSet.setupAppStyle(context)
        val dataSets: MutableList<ILineDataSet> = ArrayList()
        dataSets.add(dataSet)
        return LineData(dataSets)
    }
    private suspend fun getCostPerKwhChartDataSets(readings:List<ElectricReading>): LineData {
        val entries: MutableList<Entry> = ArrayList()
        readings.forEach { r ->
            val cost = calculateEnergyCosts(r.kwAggConsumption, meter.value!!)
            val total = if (cost.energy > 0)
                cost.total
            else
                0f
            entries.add(Entry(r.kwAggConsumption, total))
        }
        val dataSet = LineDataSet(entries, "Gasto vs kWh")
        dataSet.setupAppStyle(context)
        val dataSets: MutableList<ILineDataSet> = ArrayList()
        dataSets.add(dataSet)
        return LineData(dataSets)
    }

    private fun getPeriodDataSets(): BarData {
        val periods = dao.getMeterAllPeriods(meter.value!!.code)
        val entries: MutableList<BarEntry> = ArrayList()

        periods.sortedBy { it.fromDate }.forEachIndexed{ index, period ->
            entries.add(BarEntry(index.toFloat(), period.totalKw))
        }

        val pDataSet = BarDataSet(entries, context.getString(R.string.label_billing_period))
        pDataSet.color = ContextCompat.getColor(context, R.color.primaryColor)
        pDataSet.valueTypeface = ResourcesCompat.getFont(context, R.font.source_sans_pro_semibold)
        pDataSet.valueTextSize = 10f
        val dataSets: MutableList<IBarDataSet> = ArrayList()
        dataSets.add(pDataSet)
        val data = BarData(dataSets)
        data.barWidth = 0.8f

        return  data
    }

    suspend fun getPeriodDataLabels(): MutableList<String>  = withContext(Dispatchers.IO){
        val periods = dao.getMeterAllPeriods(meter.value!!.code)
        val labels: MutableList<String> = ArrayList()
        val timeFormat = SimpleDateFormat("MMMyy", Locale.getDefault())

        periods.sortedBy { it.fromDate }.forEach{ period ->
            labels.add(timeFormat.format(period.fromDate))
        }
        return@withContext labels
    }
}