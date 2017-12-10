package com.nicrosoft.consumoelectrico.fragments.main.chart_helpers;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

/**
 * Created by Eder Xavier Rojas on 20/01/2017.
 */

public class MyYAxisValueFormatter implements IAxisValueFormatter {
    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        return String.format( "%.1f",value);
    }
}
