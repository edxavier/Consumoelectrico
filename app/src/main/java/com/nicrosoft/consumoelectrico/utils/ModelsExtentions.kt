package com.nicrosoft.consumoelectrico.utils

import com.nicrosoft.consumoelectrico.data.entities.ElectricMeter
import com.nicrosoft.consumoelectrico.data.entities.ElectricReading
import com.nicrosoft.consumoelectrico.ui2.ElectricViewModel


suspend fun ElectricMeter.getLastReading(viewModel:ElectricViewModel): List<ElectricReading> {
    var lastReadings:List<ElectricReading> = ArrayList()
    val period = viewModel.getLastElectricPeriod(this.code)
    if (period!=null){
        lastReadings = viewModel.getLastTwoElectricReadings(period.code)
    }
    return lastReadings
}