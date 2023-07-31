package com.nicrosoft.consumoelectrico.screens.states

import com.nicrosoft.consumoelectrico.data.entities.ElectricMeter

data class MeterListState(
    var isLoading:Boolean = true,
    var meterList: List<ElectricMeter> = listOf(),
)