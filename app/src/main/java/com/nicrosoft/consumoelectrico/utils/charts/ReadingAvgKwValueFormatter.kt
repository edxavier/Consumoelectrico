package com.nicrosoft.consumoelectrico.utils.charts

import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.formatter.ValueFormatter
import com.nicrosoft.consumoelectrico.utils.toOneDecimalPlace
import com.nicrosoft.consumoelectrico.utils.toTwoDecimalPlace

class ReadingAvgKwValueFormatter: ValueFormatter() {
    override fun getAxisLabel(value: Float, axis: AxisBase?): String {
        return (value).toOneDecimalPlace()
    }
}