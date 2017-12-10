package com.nicrosoft.consumoelectrico.fragments.main.chart_helpers;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.util.Locale;

/**
 * Created by Eder Xavier Rojas on 20/01/2017.
 */

public class MyValueFormatter implements IValueFormatter {
    @Override
    public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
        return String.format(Locale.getDefault(), "%.0f",value);

    }
}
