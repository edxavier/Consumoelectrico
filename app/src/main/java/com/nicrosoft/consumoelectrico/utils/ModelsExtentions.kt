package com.nicrosoft.consumoelectrico.utils

import com.nicrosoft.consumoelectrico.data.entities.ElectricMeter
import com.nicrosoft.consumoelectrico.data.entities.ElectricReading
import com.nicrosoft.consumoelectrico.viewmodels.ElectricViewModel



fun ElectricReading.getConsumptionProjection(meter:ElectricMeter): Float{
    val days = this.consumptionHours / 24
    val daysLeft = meter.periodLength - days
    return daysLeft * (this.kwAvgConsumption * 24) + this.kwAggConsumption
}