package com.nicrosoft.consumoelectrico.utils

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import com.nicrosoft.consumoelectrico.R
import kotlinx.android.synthetic.main.marker.view.*

@SuppressLint("ViewConstructor")
class MyMarkerView(private val ctx:Context, private val layout:Int): MarkerView(ctx, layout){
    var days:Float = 0f
    @SuppressLint("SetTextI18n")
    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        val daysLabel = ctx.getString(R.string.label_days)
        val energy = ctx.getString(R.string.energy)
        marker_kwh.text = "$energy: ${e!!.y.toTwoDecimalPlace()} kWh"
        marker_days.text = "$daysLabel: ${(e.x/24).toTwoDecimalPlace()}"
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

