package com.nicrosoft.consumoelectrico.utils.charts

import android.annotation.SuppressLint
import android.content.Context
import android.widget.TextView
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import com.nicrosoft.consumoelectrico.R
import com.nicrosoft.consumoelectrico.utils.toTwoDecimalPlace

@SuppressLint("ViewConstructor")
class CostVsDayMarkerView(private val ctx:Context, private val layout:Int): MarkerView(ctx, layout){
    var days:Float = 0f
    @SuppressLint("SetTextI18n")
    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        val marker_kwh = findViewById<TextView>(R.id.marker_kwh)
        val marker_days = findViewById<TextView>(R.id.marker_days)
        marker_kwh.text = "Gasto: C$ ${e!!.y.toTwoDecimalPlace()}"
        marker_days.text = "Dias: ${(e.x).toTwoDecimalPlace()}"
        days = (e.x/24)
        super.refreshContent(e, highlight)
    }

    override fun getOffset(): MPPointF {
        return if(days<10)
            MPPointF(((width / 6)).toFloat(), (-height-100).toFloat())
        else
            MPPointF((-(width)).toFloat(), (-height).toFloat())
    }
}

