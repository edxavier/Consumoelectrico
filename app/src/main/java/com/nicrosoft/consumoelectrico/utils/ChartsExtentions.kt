package com.nicrosoft.consumoelectrico.utils

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.LineDataSet
import com.nicrosoft.consumoelectrico.R


fun LineChart.setupLineChartStyle(){
    val black75 = ContextCompat.getColor(context, R.color.md_black_1000_75)
    val bgColor = ContextCompat.getColor(context, R.color.md_grey_100)
    val typeFaceBold = ResourcesCompat.getFont(context, R.font.source_sans_pro_semibold)
    val typeFaceRegular = ResourcesCompat.getFont(context, R.font.source_sans_pro)
    with(this){
        setNoDataText(context.getString(R.string.no_data_chart))
        setNoDataTextColor(black75)
        description.isEnabled = false
        setNoDataTextTypeface(typeFaceBold)
        setExtraOffsets(5f, 15f, 0f, 0f);
        setBackgroundColor(bgColor)
        setDrawBorders(false)
        setTouchEnabled(true)
        animateXY(500, 0);
        setDrawGridBackground(false);
        with(xAxis){
            valueFormatter = HoursValueFormatter()
            position = XAxis.XAxisPosition.BOTTOM
            setDrawGridLines(false)
            setDrawLabels(true)
            textColor = black75
            axisLineColor = black75
            granularity = 1f
            typeface = typeFaceBold
        }
        axisLeft.setDrawGridLines(false)
        axisLeft.axisMinimum = 0f
        axisRight.isEnabled = false
        axisLeft.typeface = typeFaceBold
    }
}

fun LineDataSet.setupAppStyle(context: Context){
    with(this){
        mode = LineDataSet.Mode.CUBIC_BEZIER
        setDrawValues(false)
        color = ContextCompat.getColor(context, R.color.secondaryDarkColor)
        setDrawCircleHole(false)
        setCircleColor(ContextCompat.getColor(context, R.color.secondaryDarkColor))
        circleRadius = 1.5f
        highLightColor = ContextCompat.getColor(context, R.color.primaryDarkColor)
    }
}