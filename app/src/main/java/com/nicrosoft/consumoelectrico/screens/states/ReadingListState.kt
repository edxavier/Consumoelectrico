package com.nicrosoft.consumoelectrico.screens.states

import com.nicrosoft.consumoelectrico.data.entities.ElectricBillPeriod
import com.nicrosoft.consumoelectrico.data.entities.ElectricReading

data class ReadingListState(
    var isLoading:Boolean = true,
    var readingList: List<ElectricReading> = listOf(),
    var periods: List<ElectricBillPeriod> = listOf(),
)