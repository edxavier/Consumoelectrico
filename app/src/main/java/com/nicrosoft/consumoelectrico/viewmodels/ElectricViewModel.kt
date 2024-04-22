package com.nicrosoft.consumoelectrico.viewmodels

import android.content.Context
import android.graphics.Typeface
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import com.nicrosoft.consumoelectrico.screens.states.MeterListState
import com.nicrosoft.consumoelectrico.screens.states.ReadingListState
import com.nicrosoft.consumoelectrico.utils.charts.setupAppStyle
import com.nicrosoft.consumoelectrico.utils.getConsumptionProjection
import com.nicrosoft.consumoelectrico.utils.hoursSinceDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import kotlin.time.ExperimentalTime

class ElectricViewModel(val ctx: Context, private val dao:ElectricMeterDAO) : ViewModel() {

    var expandedFab by mutableStateOf(true)
    var firstVisible by mutableStateOf(0)
    private val _meters = MutableLiveData<List<ElectricMeter>>()

    private val _meterUiState = MutableStateFlow(MeterListState())
    val meterUiState: StateFlow<MeterListState> = _meterUiState.asStateFlow()

    private val _readingUiState = MutableStateFlow(ReadingListState())
    val readingUiState: StateFlow<ReadingListState> = _readingUiState.asStateFlow()

    var meter = ElectricMeter(name = "", code = "")

    fun selectedMeter(_meter: ElectricMeter) { meter = _meter }

    fun getElectricMeterList() {
        viewModelScope.launch(Dispatchers.IO) {
            val meters = dao.getMeters()
            _meterUiState.update { state ->
                state.copy(
                    meterList = meters,
                    isLoading = false
                )
            }
        }
    }
    suspend fun getAllPeriods(meterCode: String) = withContext(Dispatchers.IO){
        val periods = dao.getAllPeriods(meterCode)
        _readingUiState.update { state ->
            state.copy(
                periods = periods,
                isLoading = false
            )
        }
        return@withContext periods
    }

    @OptIn(ExperimentalTime::class)
    suspend fun undoLastPeriod(meterCode: String) = withContext(Dispatchers.IO){
        val periods = dao.getAllPeriods(meterCode)
        if(periods.size>=2){
            val current = periods[0]
            val previous = periods[1]
            val currentPeriodReadings = getMeterPeriodReadings(current.code)
            val latestPreviousPeriodReading = getLastPeriodReading(previous.code)

            currentPeriodReadings.forEach {
                it.periodCode = previous.code
                dao.updateElectricReading(it)
            }
            dao.deleteBillingPeriod(current)
            previous.active = true
            if(currentPeriodReadings.isNotEmpty())
                previous.toDate = currentPeriodReadings.first().readingDate
            dao.updatePeriod(previous)

            latestPreviousPeriodReading?.let {
                recomputeLaterReadings(latestPreviousPeriodReading, restartAggConsumption = false)
            }
            updatePeriodTotals(previous)
        }else if(periods.size==1){
            dao.deleteBillingPeriod(periods[0])
        }
        return@withContext periods
    }

    suspend fun getMeterReadings(meterCode: String, allReadings: Boolean = false): List<ElectricReading> = withContext(Dispatchers.IO){
        var readings: List<ElectricReading> = listOf()
        _readingUiState.update { state ->
            state.copy(isLoading = true)
        }
        readings = if(allReadings){
            getMeterAllReadings(meterCode)
        }else{
            val latestPeriod = getMeterLatestPeriod(meterCode)
            if (latestPeriod!=null){
                getMeterPeriodReadings(latestPeriod.code)
            }else {
                listOf()
            }
        }
        _readingUiState.update { state ->
            state.copy(
                readingList = readings,
                isLoading = false
            )
        }
        return@withContext readings
    }
    private fun getMeterAllReadings(meterCode:String) = dao.getAllMeterReadings(meterCode)
    private suspend fun getMeterPeriodReadings(periodCode:String) = withContext(Dispatchers.IO){ dao.getPeriodMetersReadings(periodCode) }


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
    suspend fun getMeterLatestPeriod(meterCode: String) = withContext(Dispatchers.IO){ dao.getLastElectricPeriod(meterCode) }

    suspend fun validatedReadingValue(readingDate: Date, readingValue:Float, meterCode: String):Boolean = withContext(Dispatchers.IO){
        val future = dao.countInvalidFutureReadings(readingDate, readingValue, meterCode)
        val past = dao.countInvalidPastReadings(readingDate, readingValue, meterCode)
        return@withContext future <= 0 && past <=0
    }

    @ExperimentalTime
    suspend fun savedReading(reading: ElectricReading, meterCode: String, terminatePeriod:Boolean) = withContext(Dispatchers.IO){
        val period = getMeterLatestPeriod(meterCode)
        period?.let { p ->
            reading.periodCode = p.code
            reading.meterCode = meterCode
            val previous = dao.getPreviousReading(reading.periodCode!!, reading.readingDate)
            val next = dao.getNextReading(reading.periodCode!!, reading.readingDate)
            if(previous!=null){
                val isFirstPeriodReading = previous.periodCode != reading.periodCode
                computeReading(reading, previous, next, period, isFirstPeriodReading)
                updatePeriodTotals(period)
                if(terminatePeriod) {terminatePeriod(reading, period, meterCode)}
            }else{
                //No se encontro lecturas anterirores a la neuva en este periodo, esta pasa a ser la primera
                //Cargar ultima lectura del periodo aanterior
                val previousReading = dao.getLastMeterReading(meterCode, reading.readingDate)
                computeReading(reading, previousReading!!, next, period, true)
                updatePeriodTotals(period)
            }
            val latestRead = getMeterReadings(meterCode).firstOrNull()
            if(latestRead!=null)
                period.toDate = latestRead.readingDate
            dao.updatePeriod(period)
            updatePeriodTotals(period)
        }?:run{
            //Si es el primer periodo crear de cero
            createFirstPeriod(reading,meterCode)
        }
    }

    private fun createFirstPeriod(reading: ElectricReading, meterCode: String){
        val newPeriod = ElectricBillPeriod(fromDate = reading.readingDate, meterCode = meterCode, toDate = reading.readingDate)
        dao.savePeriod(newPeriod)
        val period = dao.getLastElectricPeriod(meterCode)
        period?.let{
            reading.periodCode = period.code
            reading.meterCode = meterCode
            dao.saveReading(reading)
        }
    }

    @ExperimentalTime
    private fun computeReading(current:ElectricReading, previous:ElectricReading,
                               next:ElectricReading?, period:ElectricBillPeriod, isFirstPeriodReading:Boolean){
        //inicializar  variable p, para calcular las horas desde que inicio el
        // periodo hasta la fecha de la lectura actual
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
        dao.updatePeriod(period)
        updatePeriodTotals(period)
        updatePeriodTotals(newPeriod)
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
        updatePeriodTotals(getMeterLatestPeriod(reading.meterCode?:"")!!)
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
        val pCode = reading.periodCode
        val previous = dao.getPreviousReading(reading.periodCode!!, reading.readingDate)
        val next = dao.getNextReading(reading.periodCode!!, reading.readingDate)
        var previousPeriodLastReading:ElectricReading? = null
        if (previous==null)
            previousPeriodLastReading = dao.getLastMeterReading(reading.meterCode!!, reading.readingDate)
        recomputeAndUpdate(previous, next, previousPeriodLastReading)
        dao.deleteElectricReading(reading)
        updatePeriodTotals(getMeterLatestPeriod(reading.meterCode?:"")!!)

    }

    suspend fun updatePeriodTotals(period:ElectricBillPeriod): ElectricBillPeriod = withContext(Dispatchers.IO){
        period.totalKw = dao.getTotalPeriodKw(period.code)
        period.totalBill = calculateEnergyCosts(period.totalKw, meter).total
        dao.updatePeriod(period)
        return@withContext dao.getPeriod(period.code)
    }

    @ExperimentalTime
    private fun recomputeLaterReadings(reading: ElectricReading, restartAggConsumption: Boolean=true){
        Log.w("EDER", "RESET COUTNERS $restartAggConsumption ${reading.readingValue}")
        val laterReadings = dao.getReadingsAfter(reading.periodCode!!, reading.readingDate)
        laterReadings.forEachIndexed { index, electricReading ->
            //Log.e("EDER", "RECALCULANDO lecturas")
            //Recalcular lecturas despues de la lectura pasada, que solo seria en caso de que esta sea la primer lectura de la hsitoria del medidor
            if(index == 0){
                if(restartAggConsumption) {
                    // Reset el conteo de consumo
                    electricReading.consumptionHours = electricReading.readingDate.hoursSinceDate(reading.readingDate).toFloat()
                    electricReading.kwAggConsumption = electricReading.kwConsumption
                    electricReading.kwAvgConsumption = electricReading.kwAggConsumption / electricReading.consumptionHours
                }else{
                    // continual el conteo de consumo
                    electricReading.consumptionHours = electricReading.readingDate.hoursSinceDate(reading.readingDate).toFloat() + reading.consumptionHours
                    // electricReading.consumptionPreviousHours = electricReading.readingDate.hoursSinceDate(reading.readingDate).toFloat()
                    electricReading.kwAggConsumption = electricReading.kwConsumption + reading.kwAggConsumption
                    electricReading.kwAvgConsumption = electricReading.kwAggConsumption / electricReading.consumptionHours
                }
            }else{
                val prev = laterReadings[index-1]
                if(restartAggConsumption)
                    electricReading.consumptionHours = electricReading.readingDate.hoursSinceDate(reading.readingDate).toFloat()
                else
                    electricReading.consumptionHours = electricReading.readingDate.hoursSinceDate(reading.readingDate).toFloat() + reading.consumptionHours
                // electricReading.consumptionPreviousHours = electricReading.readingDate.hoursSinceDate(prev.readingDate).toFloat()
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
        return expenses
    }

    suspend fun getLineChartData(period: ElectricBillPeriod): LineChartDataSets = withContext(Dispatchers.IO){
        val dSets = LineChartDataSets()
        val periods = dao.getAllPeriods(period.meterCode)
        val periodBefore =  if (periods.size>1)
            periods[1]
        else
            null
        val readingsBefore = if(periodBefore!=null) {
            dao.getPeriodReadings(periodBefore.code)
        }else
            null
        val readings = dao.getPeriodReadings(period.code)
        val allReadings = dao.getAllMeterReadingsAsc(period.meterCode)
        dSets.consumptionDs = getConsumptionChartDataSets(readings, readingsBefore)
        dSets.dailyAvgDs = getAvgConsumptionChartDataSets(readings)
        dSets.dailyAvgHist = getAvgConsumptionHistChartDataSets(periods.reversed())
        dSets.costPerDayDs = getCostPerDayChartDataSets(readings)
        dSets.costPerKwDs = getCostPerKwhChartDataSets(readings)
        dSets.periodDs = getPeriodDataSets()
        return@withContext dSets
    }

    private fun getConsumptionProjectionDataSet(reading: ElectricReading, color:Int): LineDataSet{
        val entries: MutableList<Entry> = ArrayList()
        if(reading.kwAggConsumption < meter.maxKwLimit){
            val projection = reading.getConsumptionProjection(meter)
            entries.add(Entry(reading.consumptionHours, reading.kwAggConsumption))
            entries.add(Entry((meter.periodLength*24).toFloat(), projection))
        }
        val dataSet = LineDataSet(entries, "Proyeccion")
        dataSet.setupAppStyle(ctx)
        dataSet.enableDashedLine(15f, 15f, 0f)
        dataSet.color = color
        return dataSet
    }

    private fun getConsumptionChartDataSets(readings:List<ElectricReading>, readingsBefore:List<ElectricReading>?): LineData {
        val entries: MutableList<Entry> = ArrayList()
        val entriesBefore: MutableList<Entry> = ArrayList()

        entries.add(Entry(0f,0f))
        entriesBefore.add(Entry(0f,0f))
        readings.forEach { r -> entries.add(Entry(r.consumptionHours, r.kwAggConsumption)) }
        readingsBefore?.forEach { r -> entriesBefore.add(Entry(r.consumptionHours, r.kwAggConsumption)) }

        val dataSet = LineDataSet(entries, ctx.getString(R.string.chart_legend_accumulated))
        val dataSetBefore = LineDataSet(entriesBefore, ctx.getString(R.string.previous_period))

        dataSet.setupAppStyle(ctx)
        dataSet.mode = LineDataSet.Mode.CUBIC_BEZIER
        dataSet.cubicIntensity = 0.15f
        dataSetBefore.setupAppStyle(ctx)
        dataSetBefore.enableDashedLine(8f, 7f, 0f)
        dataSetBefore.color = ContextCompat.getColor(ctx, R.color.md_grey_500)
        dataSetBefore.setDrawCircles(false)
        dataSetBefore.isHighlightEnabled = false
        dataSetBefore.mode = LineDataSet.Mode.CUBIC_BEZIER
        dataSetBefore.cubicIntensity = 0.15f

        val dataSets: MutableList<ILineDataSet> = ArrayList()
        dataSets.add(dataSet)
        dataSets.add(dataSetBefore)

        if(readings.isNotEmpty()){
            val projectionDs = getConsumptionProjectionDataSet(readings.last(),
                    ContextCompat.getColor(ctx, R.color.md_blue_400))
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
        val dailyDataSet = LineDataSet(dailyEntries, ctx.getString(R.string.chart_legend_avg))
        val hourlyDataSet = LineDataSet(hourlyEntries, "Promedio por hora")
        dailyDataSet.setupAppStyle(ctx)
        dailyDataSet.mode = LineDataSet.Mode.CUBIC_BEZIER
        dailyDataSet.cubicIntensity = 0.12f
        hourlyDataSet.setupAppStyle(ctx)
        hourlyDataSet.color = ContextCompat.getColor(ctx, R.color.md_teal_500)
        val dataSets: MutableList<ILineDataSet> = ArrayList()
        dataSets.add(dailyDataSet)
        //dataSets.add(hourlyDataSet)
        return LineData(dataSets)
    }

    private fun getAvgConsumptionHistChartDataSets(periods:List<ElectricBillPeriod>): LineData {
        val dailyEntries: MutableList<Entry> = ArrayList()
        periods.forEachIndexed { i, p ->
            val totalHours = p.toDate.hoursSinceDate(p.fromDate)
            val kwAvgConsumption = p.totalKw / totalHours
            if (!kwAvgConsumption.isNaN())
                dailyEntries.add(Entry((i+1).toFloat(), (kwAvgConsumption*24)))
        }
        val dailyDataSet = LineDataSet(dailyEntries, ctx.getString(R.string.chart_legend_avg))
        dailyDataSet.setupAppStyle(ctx)
        dailyDataSet.mode = LineDataSet.Mode.CUBIC_BEZIER
        dailyDataSet.cubicIntensity = 0.15f
        // dailyDataSet.setDrawCircles(false)
        val dataSets: MutableList<ILineDataSet> = ArrayList()
        dataSets.add(dailyDataSet)
        return LineData(dataSets)
    }

    private suspend fun getCostPerDayChartDataSets(readings:List<ElectricReading>): LineData {
        val entries: MutableList<Entry> = ArrayList()
        readings.forEach { r ->
            val cost = calculateEnergyCosts(r.kwAggConsumption, meter)
            val total = if (cost.energy > 0)
                cost.total
            else
                0f
            entries.add(Entry( r.consumptionHours, total))
        }
        val dataSet = LineDataSet(entries, "Gasto vs dias")
        dataSet.setupAppStyle(ctx)
        dataSet.mode = LineDataSet.Mode.CUBIC_BEZIER
        dataSet.cubicIntensity = 0.15f
        val dataSets: MutableList<ILineDataSet> = ArrayList()
        dataSets.add(dataSet)
        return LineData(dataSets)
    }
    private suspend fun getCostPerKwhChartDataSets(readings:List<ElectricReading>): LineData {
        val entries: MutableList<Entry> = ArrayList()
        readings.forEach { r ->
            val cost = calculateEnergyCosts(r.kwAggConsumption, meter)
            val total = if (cost.energy > 0)
                cost.total
            else
                0f
            entries.add(Entry(r.kwAggConsumption, total))
        }
        val dataSet = LineDataSet(entries, "Gasto vs kWh")
        dataSet.setupAppStyle(ctx)
        dataSet.setupAppStyle(ctx)
        dataSet.mode = LineDataSet.Mode.CUBIC_BEZIER
        val dataSets: MutableList<ILineDataSet> = ArrayList()
        dataSets.add(dataSet)
        return LineData(dataSets)
    }

    private fun getPeriodDataSets(): BarData {
        val periods = dao.getAllPeriods(meter.code)
        val entries: MutableList<BarEntry> = ArrayList()

        periods.sortedBy { it.fromDate }.forEachIndexed{ index, period ->
            entries.add(BarEntry(index.toFloat(), period.totalKw))
        }

        val pDataSet = BarDataSet(entries, ctx.getString(R.string.label_billing_period))
        pDataSet.color = ContextCompat.getColor(ctx, R.color.primaryColor)
        pDataSet.valueTypeface = try{ResourcesCompat.getFont(ctx, R.font.source_sans_pro_semibold)}catch (e:Exception){Typeface.DEFAULT}
        pDataSet.valueTextSize = 10f
        val dataSets: MutableList<IBarDataSet> = ArrayList()
        dataSets.add(pDataSet)
        val data = BarData(dataSets)
        data.barWidth = 0.8f

        return  data
    }

    suspend fun getPeriodDataLabels(): MutableList<String>  = withContext(Dispatchers.IO){
        val labels: MutableList<String> = ArrayList()
        val timeFormat = SimpleDateFormat("MMMyy", Locale.getDefault())
        try {
            val periods = dao.getAllPeriods(meter.code)
            periods.sortedBy { it.fromDate }.forEach{ period ->
                labels.add(timeFormat.format(period.fromDate))
            }
        }catch (e:Exception){}

        return@withContext labels
    }
}