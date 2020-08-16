package com.nicrosoft.consumoelectrico.fragments.main.chart_helpers

import android.content.Context
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.Chart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.realm.implementation.RealmBarDataSet
import com.github.mikephil.charting.data.realm.implementation.RealmLineDataSet
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.nicrosoft.consumoelectrico.R
import com.nicrosoft.consumoelectrico.realm.Lectura
import com.nicrosoft.consumoelectrico.utils.KwValueFormatter
import com.pixplicity.easyprefs.library.Prefs
import io.realm.RealmResults
import java.util.*

/**
 * Created by Eder Xavier Rojas on 23/01/2017.
 */
object ChartStyler {
    fun setup(chart: Chart<*>, context: Context, drawAvgLimit: Boolean?): Chart<*> {
        var kwLimit = 150f
        var periodDays = 30f
        try {
            kwLimit = Prefs.getString("kw_limit", "150").toFloat()
            periodDays = Prefs.getString("period_lenght", "30").toFloat()
        } catch (ignored: Exception) {
        }
        val avgLimit = kwLimit / periodDays

        // no description text
        chart.description.isEnabled = false
        chart.setNoDataText("Sin datos para mostrar")

        // enable touch gestures
        chart.setTouchEnabled(true)
        chart.xAxis.setDrawGridLines(false)
        chart.xAxis.position = XAxis.XAxisPosition.TOP
        chart.xAxis.textColor = context.resources.getColor(R.color.md_black_1000_75)
        chart.xAxis.setDrawLabels(true)
        chart.xAxis.axisLineColor = context.resources.getColor(R.color.md_black_1000_75)
        chart.legend.textColor = context.resources.getColor(R.color.md_black_1000_75)
        chart.legend.isWordWrapEnabled = true
        chart.xAxis.setDrawLabels(true)
        chart.setExtraOffsets(5f, 15f, 0f, 0f)
        chart.xAxis.axisLineColor = context.resources.getColor(R.color.md_black_1000_75)
        chart.animateXY(500, 0)
        return if (chart is LineChart) {
            val mChart = chart
            mChart.setGridBackgroundColor(context.resources.getColor(R.color.md_cyan_800))
            mChart.axisLeft.textColor = context.resources.getColor(R.color.md_black_1000_75)
            mChart.setDrawBorders(false)
            mChart.setBorderColor(context.resources.getColor(R.color.md_black_1000_50))
            mChart.axisLeft.axisLineColor = context.resources.getColor(R.color.md_cyan_800)
            mChart.axisRight.axisLineColor = context.resources.getColor(R.color.md_cyan_800)
            mChart.axisRight.setDrawLabels(false)
            mChart.setDrawGridBackground(false)
            // enable scaling and dragging
            mChart.isDragEnabled = true
            mChart.setScaleEnabled(true)
            // if disabled, scaling can be done on x- and y-axis separately
            mChart.setPinchZoom(false)
            mChart.axisRight.isEnabled = false
            val leftAxis = mChart.axisLeft
            leftAxis.gridColor = context.resources.getColor(R.color.md_black_1000)
            leftAxis.setDrawZeroLine(false)
            leftAxis.valueFormatter = KwValueFormatter()
            leftAxis.axisMinimum = 0f
            leftAxis.setDrawGridLines(false)

            //Esta linea se vera luego de pasar el limite perido
            if (!drawAvgLimit!!) {
                val upperLimit2 = LimitLine(kwLimit, "")
                upperLimit2.lineWidth = 1.3f
                upperLimit2.textColor = context.resources.getColor(R.color.md_black_1000_75)
                upperLimit2.enableDashedLine(15f, 15f, 0f)
                upperLimit2.labelPosition = LimitLine.LimitLabelPosition.RIGHT_TOP
                upperLimit2.textSize = 9f
                upperLimit2.lineColor = context.resources.getColor(R.color.md_amber_700)
                //reset all limit lines to avoid overlapping lines
                leftAxis.removeAllLimitLines()
                leftAxis.addLimitLine(upperLimit2)
            } else {
                val avgUpperLimit = LimitLine(avgLimit, "")
                avgUpperLimit.lineWidth = 0.9f
                avgUpperLimit.textColor = context.resources.getColor(R.color.md_black_1000_75)
                avgUpperLimit.enableDashedLine(15f, 15f, 0f)
                avgUpperLimit.labelPosition = LimitLine.LimitLabelPosition.RIGHT_TOP
                avgUpperLimit.textSize = 9f
                avgUpperLimit.lineColor = context.resources.getColor(R.color.md_amber_700)
                leftAxis.removeAllLimitLines()
                leftAxis.addLimitLine(avgUpperLimit)
            }
            mChart
        } else {
            val mChart = chart as BarChart
            mChart.setVisibleXRangeMaximum(6f)
            mChart
        }
    }

    @JvmStatic
    fun drawAvgLimitLine(context: Context): LineDataSet {
        val avgLimitDataset: MutableList<Entry> = ArrayList()
        var kwLimit = 150f
        var periodDays = 30f
        try {
            kwLimit = Prefs.getString("kw_limit", "150").toFloat()
            periodDays = Prefs.getString("period_lenght", "30").toFloat()
        } catch (ignored: Exception) {
        }
        var i = 0
        while (i <= periodDays) {
            val limit = kwLimit / periodDays
            avgLimitDataset.add(Entry(i.toFloat(), limit))
            i++
        }
        val title = context.resources.getString(R.string.chart_legend_avg_limit)
        val avgLimitDataSet = LineDataSet(avgLimitDataset, title)
        avgLimitDataSet.setDrawCircles(false)
        avgLimitDataSet.lineWidth = 1.2f
        avgLimitDataSet.setDrawValues(false)
        avgLimitDataSet.isHighlightEnabled = false
        avgLimitDataSet.color = context.resources.getColor(R.color.md_yellow_700)
        return avgLimitDataSet
    }

    @JvmStatic
    fun drawPeriodLimitLine(context: Context): LineDataSet {
        val limit_dataset: MutableList<Entry> = ArrayList()
        var kw_limit = 150f
        var period_days = 30f
        try {
            kw_limit = Prefs.getString("kw_limit", "150").toFloat()
            period_days = Prefs.getString("period_lenght", "30").toFloat()
        } catch (ignored: Exception) {
        }
        var i = 0
        while (i <= period_days) {
            limit_dataset.add(Entry(i.toFloat(), kw_limit))
            i++
        }
        val title = context.resources.getString(R.string.chart_legend_period_limit)
        val periodDataSet = LineDataSet(limit_dataset, title)
        periodDataSet.setDrawCircles(false)
        periodDataSet.lineWidth = 1.2f
        periodDataSet.setDrawValues(false)
        periodDataSet.isHighlightEnabled = false
        periodDataSet.color = context.resources.getColor(R.color.md_amber_700)
        return periodDataSet
    }

    fun setBardata(barDataset: RealmBarDataSet<*>, context: Context): BarData {
        val title = context.resources.getString(R.string.chart_legend_bar)
        barDataset.label = title
        barDataset.valueTextColor = context.resources.getColor(R.color.md_black_1000_75)
        barDataset.color = context.resources.getColor(R.color.md_blue_grey_400)
        //barDataset.setValueFormatter(new MyValueFormatter());
        barDataset.setDrawValues(false)
        val dataSets = ArrayList<IBarDataSet>()
        dataSets.add(barDataset) // add the dataset
        // create a data object with the dataset list
        return BarData(dataSets)
    }

    fun setCombinedLine(combinedLineDataSet: RealmLineDataSet<*>, context: Context): RealmLineDataSet<*> {
        val title = context.resources.getString(R.string.chart_legend_avg)
        combinedLineDataSet.label = title
        //combinedLineDataSet.setValueFormatter(new MyAvgFormatter());
        combinedLineDataSet.setDrawFilled(false)
        combinedLineDataSet.isHighlightEnabled = false
        combinedLineDataSet.lineWidth = 1.2f
        combinedLineDataSet.setDrawCircleHole(false)
        combinedLineDataSet.setCircleColor(context.resources.getColor(R.color.md_black_1000_75))
        //combinedLineDataSet.enableDashedLine(10f, 10f, 0f);
        //combinedLineDataSet.setMode(LineDataSet.Mode.STEPPED);
        //combinedLineDataSet.setCubicIntensity(0.1f);
        combinedLineDataSet.valueTextSize = 8f
        combinedLineDataSet.valueTextColor = context.resources.getColor(R.color.md_black_1000_75)
        combinedLineDataSet.color = context.resources.getColor(R.color.md_pink_500)
        combinedLineDataSet.fillColor = context.resources.getColor(R.color.md_pink_500)
        return combinedLineDataSet
    }

    @JvmStatic
    fun setAcumuladoPeriodLine(acumuladoDataSet: RealmLineDataSet<*>, context: Context): RealmLineDataSet<*> {
        val title = context.resources.getString(R.string.chart_legend_accumulated)
        acumuladoDataSet.label = title
        //acumuladoDataSet.setValueFormatter(new MyValueFormatter());
        acumuladoDataSet.setDrawFilled(false)
        acumuladoDataSet.setDrawCircleHole(false)
        acumuladoDataSet.lineWidth = 1.2f
        //acumuladoDataSet.enableDashedLine(10f, 10f, 0f);
        acumuladoDataSet.mode = LineDataSet.Mode.LINEAR
        //acumuladoDataSet.setCubicIntensity(0.1f);
        acumuladoDataSet.valueTextSize = 8f
        acumuladoDataSet.setCircleColor(context.resources.getColor(R.color.md_pink_500))
        acumuladoDataSet.valueTextColor = context.resources.getColor(R.color.md_black_1000_75)
        acumuladoDataSet.color = context.resources.getColor(R.color.md_pink_500)
        acumuladoDataSet.fillColor = context.resources.getColor(R.color.md_pink_500)
        return acumuladoDataSet
    }

    @JvmStatic
    fun setAvgPeriodLine(acumuladoDataSet: RealmLineDataSet<*>, context: Context): RealmLineDataSet<*> {
        val title = context.resources.getString(R.string.chart_legend_avg)
        acumuladoDataSet.label = title
        acumuladoDataSet.valueFormatter = KwValueFormatter()
        acumuladoDataSet.setDrawFilled(false)
        acumuladoDataSet.setDrawCircleHole(false)
        acumuladoDataSet.lineWidth = 1.2f
        //acumuladoDataSet.enableDashedLine(10f, 10f, 0f);
        acumuladoDataSet.mode = LineDataSet.Mode.LINEAR
        //acumuladoDataSet.setCubicIntensity(0.1f);
        acumuladoDataSet.valueTextSize = 9f
        acumuladoDataSet.setCircleColor(context.resources.getColor(R.color.md_pink_500))
        acumuladoDataSet.valueTextColor = context.resources.getColor(R.color.md_black_1000_75)
        acumuladoDataSet.color = context.resources.getColor(R.color.md_pink_500)
        acumuladoDataSet.fillColor = context.resources.getColor(R.color.md_pink_500)
        //acumuladoDataSet.setDrawValues(false);
        return acumuladoDataSet
    }

    @JvmStatic
    fun setProyectionPeriodLine(lecturas: RealmResults<Lectura?>, context: Context): LineDataSet {
        val lectura = lecturas.last()
        val proyection_dataset: MutableList<Entry> = ArrayList()
        var kw_limit = 150f
        var period_days = 30f
        try {
            kw_limit = Prefs.getString("kw_limit", "150").toFloat()
            period_days = Prefs.getString("period_lenght", "30").toFloat()
        } catch (ignored: Exception) {
        }
        var cp = lectura!!.consumo_acumulado
        var i = lectura.dias_periodo.toInt()
        while (i <= period_days) {
            proyection_dataset.add(Entry(i.toFloat(), cp))
            cp = cp + lectura.consumo_promedio
            i++
        }
        val title = context.resources.getString(R.string.chart_legend_estimate)
        val periodDataSet = LineDataSet(proyection_dataset, title)
        periodDataSet.setDrawCircles(false)
        //periodDataSet.setLineWidth(0.8f);
        periodDataSet.setDrawValues(false)
        periodDataSet.isHighlightEnabled = false
        periodDataSet.color = context.resources.getColor(R.color.md_lime_800)
        periodDataSet.enableDashedLine(10f, 10f, 0f)
        return periodDataSet
    }
}