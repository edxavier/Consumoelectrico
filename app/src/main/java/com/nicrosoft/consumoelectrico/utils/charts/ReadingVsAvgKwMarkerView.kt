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
import com.pixplicity.easyprefs.library.Prefs

@SuppressLint("ViewConstructor")
class ReadingVsAvgKwMarkerView(private val ctx:Context, private val layout:Int): MarkerView(ctx, layout){
    var days:Float = 0f
    @SuppressLint("SetTextI18n")
    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        val exp = ctx.getString(R.string.daily_avg)
        val cons = ctx.getString(R.string.label_billing_period)
        val marker_kwh = findViewById<TextView>(R.id.marker_kwh)
        val marker_days = findViewById<TextView>(R.id.marker_days)
        marker_kwh.text = "$exp: ${e!!.y.toTwoDecimalPlace()} kWh"
        marker_days.text = "$cons: ${(e.x).toTwoDecimalPlace()}"
        days = (e.x)
        super.refreshContent(e, highlight)
    }

    override fun getOffset(): MPPointF {
        return if(days<2)
            MPPointF(((width / 6)).toFloat(), (-height-100).toFloat())
        else
            MPPointF((-(width)).toFloat(), (-height).toFloat())
    }
}

