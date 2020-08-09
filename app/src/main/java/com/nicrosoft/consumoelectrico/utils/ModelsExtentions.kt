package com.nicrosoft.consumoelectrico.utils

import com.nicrosoft.consumoelectrico.data.entities.ElectricMeter
import com.nicrosoft.consumoelectrico.data.entities.ElectricReading
import com.nicrosoft.consumoelectrico.ui2.ElectricViewModel


suspend fun ElectricMeter.getLastReading(viewModel:ElectricViewModel): ElectricReading? {
    var lastReadings:ElectricReading? = null
    val period = viewModel.getLastPeriod(this.code)
    if (period!=null){
        lastReadings = viewModel.getLastPeriodReading(period.code)
    }
    return lastReadings
}


fun ElectricReading.estimateConsumption(meter:ElectricMeter): Float{
    val days = this.consumptionHours / 24
    val daysLeft = meter.periodLength - days
    return daysLeft * (this.kwAvgConsumption * 24) + this.kwAggConsumption
}