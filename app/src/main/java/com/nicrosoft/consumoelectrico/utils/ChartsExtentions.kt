package com.nicrosoft.consumoelectrico.utils

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.Chart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.LineDataSet
import com.nicrosoft.consumoelectrico.R


fun Chart<*>.setupLineChartStyle(){
    val mChart = if (this is LineChart)
        this as LineChart
    else
        this as BarChart

    val black75 = ContextCompat.getColor(context, R.color.md_black_1000_75)
    val bgColor = ContextCompat.getColor(context, R.color.md_grey_100)
    val typeFaceBold = ResourcesCompat.getFont(context, R.font.source_sans_pro_semibold)
    val typeFaceRegular = ResourcesCompat.getFont(context, R.font.source_sans_pro)
    with(mChart){
        setNoDataText(context.getString(R.string.no_data_chart))
        setNoDataTextColor(black75)
        description.isEnabled = false
        setNoDataTextTypeface(typeFaceBold)
        setExtraOffsets(5f, 15f, 0f, 0f);
        setBackgroundColor(bgColor)
        setDrawBorders(false)
        setTouchEnabled(true)
        marker = MyMarkerView(context, R.layout.marker)
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
            axisMinimum = 0f
        }
        axisLeft.setDrawGridLines(false)
        axisLeft.axisMinimum = 0f
        axisRight.isEnabled = false
        axisLeft.typeface = typeFaceBold
    }
}

fun Chart<*>.drawLimit(limitValue:Float) {
    val mChart = if (this is BarChart)
        this as BarChart
    else
        this as LineChart
    val typeFaceRegular = ResourcesCompat.getFont(context, R.font.source_sans_pro)
    with(mChart){
        val limit = LimitLine(limitValue, context.getString(R.string.chart_legend_period_limit))
        limit.lineWidth = 1.3f
        limit.typeface = typeFaceRegular
        limit.textColor = ContextCompat.getColor(context, R.color.md_black_1000_75)
        limit.enableDashedLine(15f, 15f, 0f)
        limit.labelPosition = LimitLine.LimitLabelPosition.LEFT_TOP
        limit.textSize = 9f
        limit.lineColor = ContextCompat.getColor(context, R.color.md_deep_purple_500)
        //reset all limit lines to avoid overlapping lines
        axisLeft.removeAllLimitLines()
        axisLeft.addLimitLine(limit)
    }
}

fun LineDataSet.setupAppStyle(context: Context){
    with(this){
        setDrawValues(false)
        color = ContextCompat.getColor(context, R.color.secondaryDarkColor)
        setDrawCircleHole(false)
        setCircleColor(ContextCompat.getColor(context, R.color.secondaryDarkColor))
        circleRadius = 1.5f
        highLightColor = ContextCompat.getColor(context, R.color.primaryDarkColor)
    }
}