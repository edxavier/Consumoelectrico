package com.nicrosoft.consumoelectrico.utils.charts

import android.annotation.SuppressLint
import android.content.Context
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import com.nicrosoft.consumoelectrico.R
import com.nicrosoft.consumoelectrico.utils.toTwoDecimalPlace
import com.pixplicity.easyprefs.library.Prefs
import kotlinx.android.synthetic.main.marker.view.*

@SuppressLint("ViewConstructor")
class CostVsKwMarkerView(private val ctx:Context, private val layout:Int): MarkerView(ctx, layout){
    var days:Float = 0f
    @SuppressLint("SetTextI18n")
    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        val symbol = Prefs.getString("price_simbol", "$")
        val exp = ctx.getString(R.string._expenses)
        val cons = ctx.getString(R.string.consumption)
        marker_kwh.text = "$exp: $symbol${e!!.y.toTwoDecimalPlace()}"
        marker_days.text = "$cons: ${(e.x).toTwoDecimalPlace()} kWh"
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

