package com.nicrosoft.consumoelectrico.data

import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.LineData

class LineChartDataSets {
    var consumptionDs: LineData = LineData()
    var costPerDayDs: LineData = LineData()
    var costPerKwDs: LineData = LineData()
    var dailyAvgDs: LineData = LineData()
    var periodDs: BarData = BarData()
}